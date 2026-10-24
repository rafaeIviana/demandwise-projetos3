package br.com.demandwise.controller;

import br.com.demandwise.model.Apartamento;
import br.com.demandwise.model.Edificacao;
import br.com.demandwise.service.ChecklistService;
import br.com.demandwise.service.DemandaService;
import br.com.demandwise.service.RelatorioService;
import br.com.demandwise.service.ResultadoDemanda;
import com.jayway.jsonpath.JsonPath;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RelatorioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String PDF = "/api/relatorio/pdf";

    @Test
    void geraPdfComEntradasMemoriaEResultadoConsistentesComCalculo() throws Exception {
        String json = """
                {
                  "apartamentos": [
                    {"areaUtil": 40.25, "potenciaInstalada": 10.125, "potenciaRecargaVe": 0.5, "possuiEstudoDeRede": false},
                    {"areaUtil": 50.5, "potenciaInstalada": 8.75, "potenciaRecargaVe": 0, "possuiEstudoDeRede": true}
                  ],
                  "potenciaIluminacao": 10.125,
                  "potenciaTomadas": 9.875,
                  "potenciasMotores": [15.25, 3.5]
                }
                """;

        MvcResult calculo = mockMvc.perform(post("/api/demanda/calcular")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andReturn();
        Number resultadoCalculo = JsonPath.read(
                calculo.getResponse().getContentAsString(), "$.demandaTotal"
        );
        double demanda = resultadoCalculo.doubleValue();
        String demandaFormatada = String.format(Locale.forLanguageTag("pt-BR"), "%.2f", demanda);

        byte[] pdf = mockMvc.perform(post(PDF).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(
                        "Content-Disposition",
                        "attachment; filename=\"relatorio-calculo-demanda.pdf\""
                ))
                .andReturn().getResponse().getContentAsByteArray();

        assertTrue(new String(pdf, 0, 5, StandardCharsets.US_ASCII).startsWith("%PDF-"));
        String texto = extrairTexto(pdf).replaceAll("\\s+", " ");
        try (PdfReader reader = new PdfReader(pdf)) {
            assertEquals(1, reader.getNumberOfPages());
        }

        assertTrue(texto.contains("MEMORIAL DE CÁLCULO DE DEMANDA"));
        assertTrue(texto.contains("Tipo de instalação: Residencial"));
        assertTrue(texto.contains("Apartamento 1: área útil 40,25 m²"));
        assertTrue(texto.contains("potência instalada 10,125 kW"));
        assertTrue(texto.contains("recarga de veículo elétrico 0,5 kW"));
        assertTrue(texto.contains("estudo de rede: não"), texto);
        assertTrue(texto.contains("Apartamento 2: área útil 50,5 m²"));
        assertTrue(texto.contains("estudo de rede: sim"));
        assertTrue(texto.contains("Potência de iluminação: 10,125 kW"));
        assertTrue(texto.contains("Potência de tomadas: 9,875 kW"));
        assertTrue(texto.contains("Motores: 15,25 kW; 3,5 kW"));
        assertTrue(texto.contains("Critério"));
        assertTrue(texto.contains("Parâmetro"));
        assertTrue(texto.contains("Valor aplicado"));
        String textoSemEspacos = texto.replaceAll("\\s+", "");
        assertTrue(textoSemEspacos.contains("apartamento[0].areaUtil=40.25"));
        assertTrue(textoSemEspacos.contains("potenciaIluminacao=10.125"));
        assertTrue(texto.contains("Demanda total da edificação: " + demandaFormatada + " kVA"));
    }

    @Test
    void geraPdfEmMultiplasPaginasComTodasAsUnidadesEPrecisaoDosFatores() throws Exception {
        StringBuilder apartamentos = new StringBuilder();
        for (int i = 0; i < 60; i++) {
            if (i > 0) {
                apartamentos.append(',');
            }
            apartamentos.append("{\"areaUtil\":").append(40 + i)
                    .append(",\"potenciaInstalada\":10}");
        }
        String json = """
                {"apartamentos":[%s],"potenciaIluminacao":10,"potenciaTomadas":10}
                """.formatted(apartamentos);

        byte[] pdf = mockMvc.perform(post(PDF).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn().getResponse().getContentAsByteArray();

        try (PdfReader reader = new PdfReader(pdf)) {
            assertTrue(reader.getNumberOfPages() > 1);
            List<String> paginas = extrairTextosPorPagina(reader);
            String texto = String.join(" ", paginas).replaceAll("\\s+", " ");
            assertTrue(texto.contains("Apartamento 1: área útil 40 m²"));
            assertTrue(texto.contains("Apartamento 60: área útil 99 m²"));
            assertTrue(texto.contains("Parâmetro"));
            assertTrue(texto.contains("numeroApartamentos=60"));
            assertTrue(texto.contains("0,6823"));
            assertTrue(texto.contains("Demanda total da edificação:"));

            long paginasComCriterios = paginas.stream()
                    .filter(pagina -> pagina.contains("QUADRO_35"))
                    .peek(pagina -> assertTrue(pagina.contains("Critério")))
                    .count();
            assertTrue(paginasComCriterios > 1,
                    "A memória extensa deve continuar em outras páginas com cabeçalho repetido.");
        }
    }

    @Test
    void naoGeraPdfQuandoChecklistEstaVazioOuIncompleto() throws Exception {
        mockMvc.perform(post(PDF)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.liberadoParaEnvio").value(false))
                .andExpect(jsonPath("$.pendencias[0]").value("Nenhum apartamento foi informado"))
                .andExpect(jsonPath("$.pendencias[1]").value("Potência de iluminação não informada"))
                .andExpect(jsonPath("$.pendencias[2]").value("Potência de tomadas não informada"));
    }

    @Test
    void naoGeraPdfComAreaInvalida() throws Exception {
        String json = """
                {
                  "apartamentos": [{"areaUtil": 0, "potenciaInstalada": 10}],
                  "potenciaIluminacao": 10,
                  "potenciaTomadas": 10
                }
                """;

        mockMvc.perform(post(PDF).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.liberadoParaEnvio").value(false))
                .andExpect(jsonPath("$.pendencias[0]").value("Apartamento 1: área útil não informada"));
    }

    @Test
    void naoGeraPdfParaTipoAindaNaoImplementado() throws Exception {
        String json = """
                {
                  "apartamentos": [{"areaUtil": 40, "potenciaInstalada": 10}],
                  "potenciaIluminacao": 10,
                  "potenciaTomadas": 10,
                  "tipoInstalacao": "COMERCIAL"
                }
                """;

        mockMvc.perform(post(PDF).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isNotImplemented());
    }

    @Test
    void devolveErroControladoQuandoGeracaoDoPdfFalha() {
        Edificacao edificacao = new Edificacao(List.of(new Apartamento(40)));
        edificacao.getApartamentos().get(0).setPotenciaInstalada(10);
        edificacao.setPotenciaIluminacao(10);
        edificacao.setPotenciaTomadas(10);

        RelatorioService relatorioService = new RelatorioService() {
            @Override
            public byte[] gerarPdf(Edificacao dados, ResultadoDemanda resultado) {
                throw new IllegalStateException("falha simulada");
            }
        };

        RelatorioController controller = new RelatorioController(
                new ChecklistService(), new DemandaService(), relatorioService
        );
        ResponseEntity<?> resposta = controller.gerarPdf(edificacao);

        assertEquals(500, resposta.getStatusCode().value());
        assertEquals(
                "Não foi possível gerar o relatório PDF. Tente novamente.",
                ((Map<?, ?>) resposta.getBody()).get("mensagem")
        );
    }

    private String extrairTexto(byte[] pdf) throws Exception {
        try (PdfReader reader = new PdfReader(pdf)) {
            return extrairTexto(reader);
        }
    }

    private String extrairTexto(PdfReader reader) throws Exception {
        return String.join("\n", extrairTextosPorPagina(reader));
    }

    private List<String> extrairTextosPorPagina(PdfReader reader) throws Exception {
        List<String> paginas = new java.util.ArrayList<>();
        PdfTextExtractor extrator = new PdfTextExtractor(reader);
        for (int pagina = 1; pagina <= reader.getNumberOfPages(); pagina++) {
            paginas.add(extrator.getTextFromPage(pagina));
        }
        return paginas;
    }
}

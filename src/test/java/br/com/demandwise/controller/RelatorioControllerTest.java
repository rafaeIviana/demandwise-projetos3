package br.com.demandwise.controller;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RelatorioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String PDF = "/api/relatorio/pdf";

    @Test
    void geraPdfComDadosDeEntradaEResultado() throws Exception {
        String json = """
                {
                  "apartamentos": [{"areaUtil": 40, "potenciaInstalada": 10}],
                  "potenciaIluminacao": 10,
                  "potenciaTomadas": 10
                }
                """;

        byte[] pdf = mockMvc.perform(post(PDF).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn().getResponse().getContentAsByteArray();

        PdfReader leitor = new PdfReader(pdf);
        String texto = new PdfTextExtractor(leitor).getTextFromPage(1);
        leitor.close();

        assertTrue(texto.contains("MEMORIAL DE CÁLCULO DE DEMANDA"));
        assertTrue(texto.contains("Apartamento 1: área útil 40,00 m²"));
        assertTrue(texto.contains("Potência de tomadas: 10,00 kW"));
        assertTrue(texto.contains("QUADRO_35"));
        assertTrue(texto.contains("Demanda total da edificação: 20,25 kVA"));
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
                .andExpect(jsonPath("$.mensagem").value("A área útil deve ser maior que zero."));
    }
}

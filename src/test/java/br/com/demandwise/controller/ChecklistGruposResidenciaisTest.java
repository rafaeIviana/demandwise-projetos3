package br.com.demandwise.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ChecklistGruposResidenciaisTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void telaNovoProjetoExpõeEntradaPorGruposResidenciais() throws Exception {
        mockMvc.perform(get("/envio"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("modo-residencial")))
                .andExpect(content().string(containsString("lista-grupos-residenciais")))
                .andExpect(content().string(containsString("Adicionar grupo")));
    }

    @Test
    void fluxoChecklistCalculaGruposSemExigirCargasDeServicoEDevolveResultadoDetalhado() throws Exception {
        String json = """
                {
                  "gruposResidenciais": [
                    {"identificacao":"Grupo 1", "tipologia":"61–65 m²", "areaMinimaM2":61, "areaMaximaM2":65, "quantidade":50},
                    {"identificacao":"Grupo 2", "tipologia":"66–70 m²", "areaMinimaM2":66, "areaMaximaM2":70, "quantidade":50}
                  ]
                }
                """;

        mockMvc.perform(post("/api/checklist/enviar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liberadoParaEnvio").value(true))
                .andExpect(jsonPath("$.resultadoDemanda.demandaTotal").value(closeTo(114.08056, 0.000001)))
                .andExpect(jsonPath("$.resultadoDemanda.detalhamento.residencial.numeroUnidades").value(100))
                .andExpect(jsonPath("$.resultadoDemanda.detalhamento.residencial.demandaResidencialBaseDrKva").value(152.0))
                .andExpect(jsonPath("$.resultadoDemanda.detalhamento.residencial.fatorCoincidenciaFc").value(closeTo(0.6823, 0.000001)))
                .andExpect(jsonPath("$.resultadoDemanda.detalhamento.residencial.demandaCoincidenteDtrKva").value(closeTo(103.7096, 0.000001)))
                .andExpect(jsonPath("$.resultadoDemanda.detalhamento.residencial.fatorSegurancaFr").value(1.1))
                .andExpect(jsonPath("$.resultadoDemanda.detalhamento.residencial.demandaResidencialFinalDrfKva").value(closeTo(114.08056, 0.000001)))
                .andExpect(jsonPath("$.resultadoDemanda.detalhamento.memoriaCalculo[0].norma").value("DIS-NOR-053"))
                .andExpect(jsonPath("$.resultadoDemanda.detalhamento.memoriaCalculo[0].revisao").value("06"))
                .andExpect(jsonPath("$.resultadoDemanda.memoriaCalculo[0].valorAplicado").value(1.47))
                .andExpect(jsonPath("$.resultadoDemanda.memoriaCalculo[2].valorAplicado").value(1.57));
    }

    @Test
    void checklistRejeitaGrupoComQuantidadeZero() throws Exception {
        String json = """
                {"gruposResidenciais":[
                  {"identificacao":"Grupo 1", "tipologia":"61–65 m²", "areaMinimaM2":61, "areaMaximaM2":65, "quantidade":0}
                ]}
                """;

        mockMvc.perform(post("/api/checklist/enviar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checklistRejeitaGrupoSemFaixaDeArea() throws Exception {
        String json = """
                {"gruposResidenciais":[
                  {"identificacao":"Grupo 1", "tipologia":"Sem faixa", "areaMinimaM2":null, "areaMaximaM2":null, "quantidade":1}
                ]}
                """;

        mockMvc.perform(post("/api/checklist/enviar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checklistRejeitaGrupoQueNaoCorrespondeAUmaUnicaFaixaDoQuadro35() throws Exception {
        String json = """
                {"gruposResidenciais":[
                  {"identificacao":"Grupo inválido", "tipologia":"65–66 m²", "areaMinimaM2":65, "areaMaximaM2":66, "quantidade":1}
                ]}
                """;

        mockMvc.perform(post("/api/checklist/enviar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.pendencias[0]", containsString("atravessa mais de uma faixa")));
    }

    @Test
    void checklistNaoAceitaEntradaSemUnidadesResidenciais() throws Exception {
        mockMvc.perform(post("/api/checklist/enviar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gruposResidenciais\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.pendencias[0]").value("Nenhum apartamento foi informado"));
    }
}

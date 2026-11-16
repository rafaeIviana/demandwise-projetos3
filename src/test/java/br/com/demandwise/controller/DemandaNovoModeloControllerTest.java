package br.com.demandwise.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DemandaNovoModeloControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiRecebeGruposResidenciaisERetornaDetalhamento() throws Exception {
        String json = """
                {
                  "gruposResidenciais": [
                    {"identificacao":"A", "tipologia":"Faixa A", "areaMinimaM2":61, "areaMaximaM2":65, "quantidade":50},
                    {"identificacao":"B", "tipologia":"Faixa B", "areaMinimaM2":66, "areaMaximaM2":70, "quantidade":50}
                  ]
                }
                """;

        mockMvc.perform(post("/api/demanda/calcular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detalhamento.residencial.numeroUnidades").value(100))
                .andExpect(jsonPath("$.detalhamento.residencial.demandaResidencialBaseDrKva").value(152.0))
                .andExpect(jsonPath("$.detalhamento.residencial.fatorCoincidenciaFc").value(0.6823));
    }

    @Test
    void apiNaoProduzTotalSeRecebeCategoriaSemRegraImplementada() throws Exception {
        String json = """
                {
                  "gruposResidenciais": [
                    {"identificacao":"A", "tipologia":"Faixa A", "areaMinimaM2":61, "areaMaximaM2":65, "quantidade":1}
                  ],
                  "cargasCondominio": {
                    "motores": [{"identificacao":"Bomba", "potencia":{"valor":12.5,"unidade":"CV"}, "quantidade":1}]
                  }
                }
                """;

        mockMvc.perform(post("/api/demanda/calcular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(
                        org.hamcrest.Matchers.containsString("motores")));
    }
}

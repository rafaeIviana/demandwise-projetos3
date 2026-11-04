package br.com.demandwise.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void paginaDashboardRenderizaOsComponentesDeComparacao() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Dados simulados")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Taxa de reprovação por mês")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Antes da adoção")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Depois da adoção")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"grafico\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"tabela-meses\"")));
    }

    @Test
    void endpointDeMetricasRetornaSerieETaxasAgregadas() throws Exception {
        mockMvc.perform(get("/api/dashboard/metricas"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.dadosSimulados").value(true))
                .andExpect(jsonPath("$.mesAdocao").value("jul/26"))
                .andExpect(jsonPath("$.meses.length()").value(12))
                .andExpect(jsonPath("$.meses[0].mes").value("jan/26"))
                .andExpect(jsonPath("$.meses[0].taxaReprovacao").value(37.5))
                .andExpect(jsonPath("$.meses[0].aposAdocao").value(false))
                .andExpect(jsonPath("$.meses[6].aposAdocao").value(true))
                .andExpect(jsonPath("$.taxaMediaAntes").value(39.0))
                .andExpect(jsonPath("$.taxaMediaDepois").value(21.9))
                .andExpect(jsonPath("$.reducaoPercentual").value(43.9));
    }
}

package br.com.demandwise.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// US-06: o envio tem que ser bloqueado no servidor, mesmo que alguém ignore a tela
@SpringBootTest
@AutoConfigureMockMvc
class EnvioValidacaoTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String ENVIAR = "/api/checklist/enviar";

    @Test
    void bloqueiaEnvioQuandoFaltaPotenciaDeIluminacao() throws Exception {
        String json = """
                {
                  "apartamentos": [{"areaUtil": 60, "potenciaInstalada": 5}],
                  "potenciaTomadas": 3
                }
                """;

        mockMvc.perform(post(ENVIAR).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.liberadoParaEnvio").value(false))
                .andExpect(jsonPath("$.pendencias[0]").value("Potência de iluminação não informada"));
    }

    @Test
    void bloqueiaEnvioSemApartamentos() throws Exception {
        String json = """
                {
                  "apartamentos": [],
                  "potenciaIluminacao": 2,
                  "potenciaTomadas": 3
                }
                """;

        mockMvc.perform(post(ENVIAR).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.liberadoParaEnvio").value(false))
                .andExpect(jsonPath("$.pendencias[0]").value("Nenhum apartamento foi informado"));
    }

    @Test
    void bloqueiaEnvioQuandoApartamentoFicaSemAreaUtil() throws Exception {
        String json = """
                {
                  "apartamentos": [{"potenciaInstalada": 5}],
                  "potenciaIluminacao": 2,
                  "potenciaTomadas": 3
                }
                """;

        mockMvc.perform(post(ENVIAR).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.pendencias[0]").value("Apartamento 1: área útil não informada"));
    }

    @Test
    void liberaEnvioQuandoTudoEstaPreenchido() throws Exception {
        String json = """
                {
                  "apartamentos": [{"areaUtil": 60, "potenciaInstalada": 5}],
                  "potenciaIluminacao": 2,
                  "potenciaTomadas": 3
                }
                """;

        mockMvc.perform(post(ENVIAR).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liberadoParaEnvio").value(true))
                .andExpect(jsonPath("$.pendencias").isEmpty());
    }

    @Test
    void telaDeEnvioCarregaComBotaoDesabilitado() throws Exception {
        mockMvc.perform(get("/envio"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"btn-enviar\"")))
                .andExpect(content().string(containsString("disabled")));
    }
}


package br.com.demandwise.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ValidacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void identificaViolacaoDaRegraDePotenciaSemEstudoDeRede() throws Exception {
        String json = """
                {
                  "areaUtil": 60,
                  "potenciaInstalada": 21,
                  "potenciaRecargaVe": 0,
                  "possuiEstudoDeRede": false
                }
                """;

        mockMvc.perform(post("/api/validacao/apartamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.alertas[0]").exists());
    }
}

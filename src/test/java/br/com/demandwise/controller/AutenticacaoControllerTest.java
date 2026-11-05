package br.com.demandwise.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "demandwise.auth.enabled=true")
class AutenticacaoControllerTest {
    @Autowired MockMvc mockMvc;
    @Test void loginValidoAbrePaginaProtegida() throws Exception {
        var login = mockMvc.perform(post("/login").param("usuario", "demo").param("senha", "DemandWise2026!"))
                .andExpect(status().is3xxRedirection()).andReturn();
        mockMvc.perform(get("/envio").session((org.springframework.mock.web.MockHttpSession) login.getRequest().getSession(false)))
                .andExpect(status().isOk()).andExpect(view().name("envio"));
    }
    @Test void loginInvalidoNaoConcedeAcesso() throws Exception {
        var login = mockMvc.perform(post("/login").param("usuario", "demo").param("senha", "incorreta"))
                .andExpect(status().isOk()).andExpect(view().name("login")).andReturn();
        mockMvc.perform(get("/envio"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
    }
    @Test void rotaProtegidaRedirecionaELogoutEncerraSessao() throws Exception {
        mockMvc.perform(get("/dashboard")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
        var login = mockMvc.perform(post("/login").param("usuario", "demo").param("senha", "DemandWise2026!"))
                .andReturn();
        var session = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession(false);
        mockMvc.perform(post("/logout").session(session)).andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/dashboard").session(session)).andExpect(status().is3xxRedirection());
    }
}

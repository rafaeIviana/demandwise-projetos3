package br.com.demandwise.controller;

import br.com.demandwise.model.Apartamento;
import br.com.demandwise.model.Edificacao;
import br.com.demandwise.projeto.ProjetoPreValidado;
import br.com.demandwise.service.CriterioAplicado;
import br.com.demandwise.service.ProjetoPreValidadoService;
import br.com.demandwise.service.ResultadoDemanda;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class PainelAnalistaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjetoPreValidadoService service;

    private ProjetoPreValidado projetoExemplo() {
        Apartamento apartamento = new Apartamento(40, 10, 2.5, true);
        Edificacao edificacao = new Edificacao(List.of(apartamento));
        edificacao.setPotenciaIluminacao(12.0);
        edificacao.setPotenciaTomadas(8.0);
        ResultadoDemanda resultado = new ResultadoDemanda(42.125, List.of(
                new CriterioAplicado("QUADRO_35", "areaUtil=40.0", "Demanda unitária 10.0 kVA", 10.0)
        ));
        return new ProjetoPreValidado(null, "Pré-validado", edificacao, resultado);
    }

    @Test
    @Order(1)
    void listagemSemProjetosApresentaMensagemInformativa() throws Exception {
        mockMvc.perform(get("/analista/projetos"))
                .andExpect(status().isOk())
                .andExpect(view().name("analista/projetos"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Nenhum projeto disponível")));
    }

    @Test
    @Order(2)
    void listagemApresentaProjetosEStatus() throws Exception {
        ProjetoPreValidado projeto = service.salvar(projetoExemplo());

        mockMvc.perform(get("/analista/projetos"))
                .andExpect(status().isOk())
                .andExpect(view().name("analista/projetos"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Projeto #" + projeto.id())))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Pré-validado")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("1 apartamento")));
    }

    @Test
    @Order(3)
    void detalhesApresentamDadosEResultadoArmazenadosSemRefazerCalculo() throws Exception {
        ProjetoPreValidado projeto = service.salvar(projetoExemplo());

        mockMvc.perform(get("/analista/projetos/" + projeto.id()))
                .andExpect(status().isOk())
                .andExpect(view().name("analista/projeto-detalhes"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("42,13")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Parâmetro")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("areaUtil=40.0")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Potência de recarga veicular")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Pré-validado")));
    }

    @Test
    @Order(4)
    void identificadorInexistenteRetornaPaginaComStatus404() throws Exception {
        mockMvc.perform(get("/analista/projetos/999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("analista/projeto-nao-encontrado"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Voltar à listagem")));
    }
}

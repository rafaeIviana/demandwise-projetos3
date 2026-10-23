
package br.com.demandwise.controller;

import br.com.demandwise.model.Apartamento;
import br.com.demandwise.model.Edificacao;
import br.com.demandwise.service.DemandaService;
import br.com.demandwise.service.ProjetoPreValidadoService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ChecklistControllerTest {

    private ChecklistController criarController() {
        return new ChecklistController(
                new DemandaService(),
                new ProjetoPreValidadoService()
        );
    }

    @Test
    void deveBloquearEnvioQuandoNaoHouverApartamentos() {
        ChecklistController controller = criarController();
        Edificacao edificacao = new Edificacao();

        var resultado = controller.verificar(edificacao);

        assertFalse(resultado.liberadoParaEnvio());
    }

    @Test
    void deveIdentificarPendenciaNoApartamento() {
        ChecklistController controller = criarController();
        Edificacao edificacao = new Edificacao();

        Apartamento apartamento = new Apartamento();
        apartamento.setAreaUtil(0);
        apartamento.setPotenciaInstalada(10);

        edificacao.setApartamentos(java.util.List.of(apartamento));
        edificacao.setPotenciaIluminacao(10);
        edificacao.setPotenciaTomadas(10);

        var resultado = controller.verificar(edificacao);

        assertFalse(resultado.liberadoParaEnvio());
        assertTrue(resultado.pendencias().contains(
                "Apartamento 1: área útil não informada"));
    }

    @Test
    void deveIdentificarPendenciaDeIluminacaoETomadas() {
        ChecklistController controller = criarController();
        Edificacao edificacao = new Edificacao();

        Apartamento apartamento = new Apartamento();
        apartamento.setAreaUtil(40);
        apartamento.setPotenciaInstalada(10);

        edificacao.setApartamentos(java.util.List.of(apartamento));
        edificacao.setPotenciaIluminacao(0);
        edificacao.setPotenciaTomadas(0);

        var resultado = controller.verificar(edificacao);

        assertFalse(resultado.liberadoParaEnvio());
        assertTrue(resultado.pendencias().contains(
                "Potência de iluminação não informada"));
        assertTrue(resultado.pendencias().contains(
                "Potência de tomadas não informada"));
    }

    @Test
    void deveLiberarEnvioQuandoChecklistEstiverCompleto() {
        ChecklistController controller = criarController();
        Edificacao edificacao = new Edificacao();

        Apartamento apartamento = new Apartamento();
        apartamento.setAreaUtil(40);
        apartamento.setPotenciaInstalada(10);

        edificacao.setApartamentos(java.util.List.of(apartamento));
        edificacao.setPotenciaIluminacao(10);
        edificacao.setPotenciaTomadas(10);

        var resultado = controller.verificar(edificacao);

        assertTrue(resultado.liberadoParaEnvio());
        assertTrue(resultado.pendencias().isEmpty());
    }

    @Test
    void deveRetornar400QuandoChecklistEstiverIncompleto() {
        ChecklistController controller = criarController();
        Edificacao edificacao = new Edificacao();

        var resposta = controller.enviar(edificacao);

        assertEquals(400, resposta.getStatusCode().value());
    }

    @Test
    void deveRetornar200QuandoChecklistEstiverCompleto() {
        ChecklistController controller = criarController();
        Edificacao edificacao = new Edificacao();

        Apartamento apartamento = new Apartamento();
        apartamento.setAreaUtil(40);
        apartamento.setPotenciaInstalada(10);

        edificacao.setApartamentos(java.util.List.of(apartamento));
        edificacao.setPotenciaIluminacao(10);
        edificacao.setPotenciaTomadas(10);

        var resposta = controller.enviar(edificacao);

        assertEquals(200, resposta.getStatusCode().value());
    }
}


package br.com.demandwise.controller;

import br.com.demandwise.model.Apartamento;
import br.com.demandwise.model.Edificacao;
import br.com.demandwise.model.TipoInstalacao;
import br.com.demandwise.projeto.ProjetoPreValidado;
import br.com.demandwise.service.ChecklistService;
import br.com.demandwise.service.DemandaService;
import br.com.demandwise.service.DisponibilidadeTipoInstalacaoService;
import br.com.demandwise.service.ProjetoPreValidadoService;
import br.com.demandwise.service.ResultadoDemanda;
import br.com.demandwise.service.ValidacaoNormativaService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChecklistControllerTest {

    private static final Validator VALIDATOR =
            Validation.buildDefaultValidatorFactory().getValidator();

    private static class DemandaServiceObservavel extends DemandaService {
        private boolean chamado;
        private RuntimeException falha;

        @Override
        public ResultadoDemanda calcularDemanda(Edificacao edificacao) {
            chamado = true;
            if (falha != null) {
                throw falha;
            }
            return super.calcularDemanda(edificacao);
        }
    }

    private ChecklistController criarController() {
        return criarController(new DemandaService(), new ProjetoPreValidadoService());
    }

    private ChecklistController criarController(
            DemandaService demandaService,
            ProjetoPreValidadoService projetoPreValidadoService) {
        return new ChecklistController(
                new ChecklistService(),
                new ValidacaoNormativaService(VALIDATOR),
                new DisponibilidadeTipoInstalacaoService(),
                demandaService,
                projetoPreValidadoService
        );
    }

    private Edificacao criarEdificacaoValida() {
        Edificacao edificacao = new Edificacao();

        Apartamento apartamento = new Apartamento();
        apartamento.setAreaUtil(40);
        apartamento.setPotenciaInstalada(10);

        edificacao.setApartamentos(java.util.List.of(apartamento));
        edificacao.setPotenciaIluminacao(10);
        edificacao.setPotenciaTomadas(10);

        return edificacao;
    }

    @Test
    void deveBloquearEnvioQuandoNaoHouverApartamentos() {
        ChecklistController controller = criarController();
        Edificacao edificacao = new Edificacao();

        var resultado = controller.verificar(edificacao);

        assertFalse(resultado.liberadoParaEnvio());
        assertTrue(resultado.pendencias().contains(
                "Nenhum apartamento foi informado"));
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
        Edificacao edificacao = criarEdificacaoValida();

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
        Edificacao edificacao = criarEdificacaoValida();

        var resultado = controller.verificar(edificacao);

        assertTrue(resultado.liberadoParaEnvio());
        assertTrue(resultado.pendencias().isEmpty());
    }

    @Test
    void deveRetornar400QuandoChecklistEstiverIncompleto() {
        DemandaServiceObservavel demandaService = new DemandaServiceObservavel();
        ProjetoPreValidadoService projetoService = new ProjetoPreValidadoService();
        ChecklistController controller = criarController(demandaService, projetoService);
        Edificacao edificacao = new Edificacao();

        var resposta = controller.enviar(edificacao);

        assertEquals(400, resposta.getStatusCode().value());
        assertTrue(projetoService.listarTodos().isEmpty());
        assertFalse(demandaService.chamado);
    }

    @Test
    void deveRetornar200QuandoChecklistEstiverCompleto() {
        ChecklistController controller = criarController();
        Edificacao edificacao = criarEdificacaoValida();

        var resposta = controller.enviar(edificacao);

        assertEquals(200, resposta.getStatusCode().value());
    }

    @Test
    void deveSalvarProjetoPreValidadoQuandoEnvioForCompleto() {
        ProjetoPreValidadoService projetoService =
                new ProjetoPreValidadoService();

        ChecklistController controller = new ChecklistController(
                new ChecklistService(),
                new ValidacaoNormativaService(VALIDATOR),
                new DisponibilidadeTipoInstalacaoService(),
                new DemandaService(),
                projetoService
        );

        Edificacao edificacao = criarEdificacaoValida();

        var resposta = controller.enviar(edificacao);

        assertEquals(1, projetoService.listarTodos().size());

        ProjetoPreValidado projeto = projetoService.listarTodos().get(0);

        assertEquals("Pré-validado", projeto.status());
        assertEquals(edificacao, projeto.edificacao());
        assertEquals(projeto.resultadoDemanda(), resposta.getBody().resultadoDemanda());
        assertEquals(
                new DemandaService().calcularDemanda(edificacao).demandaTotal(),
                projeto.resultadoDemanda().demandaTotal(),
                0.0
        );
    }

    @Test
    void tipoIndisponivelRetorna400SemCalcularOuSalvar() {
        ProjetoPreValidadoService projetoService = new ProjetoPreValidadoService();
        DemandaServiceObservavel demandaService = new DemandaServiceObservavel();
        ChecklistController controller = criarController(demandaService, projetoService);
        Edificacao edificacao = criarEdificacaoValida();
        edificacao.setTipoInstalacao(TipoInstalacao.COMERCIAL);

        var resposta = controller.enviar(edificacao);

        assertEquals(400, resposta.getStatusCode().value());
        assertTrue(resposta.getBody().pendencias().stream()
                .anyMatch(mensagem -> mensagem.contains("Comercial")
                        && mensagem.contains("disponível em breve")));
        assertFalse(demandaService.chamado);
        assertTrue(projetoService.listarTodos().isEmpty());
    }

    @Test
    void edificacaoNulaRetornaPendenciaSemCalcularOuSalvar() {
        ProjetoPreValidadoService projetoService = new ProjetoPreValidadoService();
        DemandaServiceObservavel demandaService = new DemandaServiceObservavel();
        ChecklistController controller = criarController(demandaService, projetoService);

        var resposta = controller.enviar(null);

        assertEquals(400, resposta.getStatusCode().value());
        assertTrue(resposta.getBody().pendencias().contains("Edificação não informada"));
        assertFalse(demandaService.chamado);
        assertTrue(projetoService.listarTodos().isEmpty());
    }

    @Test
    void apartamentoNuloRetornaPendenciaSemCalcularOuSalvar() {
        ProjetoPreValidadoService projetoService = new ProjetoPreValidadoService();
        DemandaServiceObservavel demandaService = new DemandaServiceObservavel();
        ChecklistController controller = criarController(demandaService, projetoService);
        Edificacao edificacao = criarEdificacaoValida();
        edificacao.setApartamentos(java.util.Arrays.asList((Apartamento) null));

        var resposta = controller.enviar(edificacao);

        assertEquals(400, resposta.getStatusCode().value());
        assertTrue(resposta.getBody().pendencias().contains("Apartamento 1 não informado"));
        assertFalse(demandaService.chamado);
        assertTrue(projetoService.listarTodos().isEmpty());
    }

    @Test
    void deveBloquearProjetoComInconsistenciaNormativaAntesDoCalculo() {
        ProjetoPreValidadoService projetoService = new ProjetoPreValidadoService();
        DemandaServiceObservavel demandaService = new DemandaServiceObservavel();
        ChecklistController controller = criarController(demandaService, projetoService);
        Edificacao edificacao = criarEdificacaoValida();
        edificacao.getApartamentos().get(0).setPotenciaInstalada(21);

        var resposta = controller.enviar(edificacao);

        assertEquals(400, resposta.getStatusCode().value());
        assertFalse(resposta.getBody().liberadoParaEnvio());
        assertTrue(resposta.getBody().pendencias().stream()
                .anyMatch(pendencia -> pendencia.contains("ultrapassa 20 kW")));
        assertTrue(projetoService.listarTodos().isEmpty());
        assertFalse(demandaService.chamado);
    }

    @Test
    void devePermitirPotenciaAcimaDe20KwQuandoHaEstudoDeRede() {
        ProjetoPreValidadoService projetoService = new ProjetoPreValidadoService();
        ChecklistController controller = criarController(
                new DemandaService(), projetoService);
        Edificacao edificacao = criarEdificacaoValida();
        Apartamento apartamento = edificacao.getApartamentos().get(0);
        apartamento.setPotenciaInstalada(21);
        apartamento.setPossuiEstudoDeRede(true);

        var resposta = controller.enviar(edificacao);

        assertEquals(200, resposta.getStatusCode().value());
        assertEquals(1, projetoService.listarTodos().size());
        assertEquals("Pré-validado", projetoService.listarTodos().get(0).status());
    }

    @Test
    void falhaNoCalculoNaoSalvaProjetoPreValidado() {
        ProjetoPreValidadoService projetoService = new ProjetoPreValidadoService();
        DemandaServiceObservavel demandaService = new DemandaServiceObservavel();
        demandaService.falha = new IllegalArgumentException("Falha no cálculo");
        ChecklistController controller = criarController(demandaService, projetoService);

        var resposta = controller.enviar(criarEdificacaoValida());

        assertEquals(400, resposta.getStatusCode().value());
        assertTrue(resposta.getBody().pendencias().contains("Falha no cálculo"));
        assertTrue(projetoService.listarTodos().isEmpty());
        assertTrue(demandaService.chamado);
    }

    @Test
    void falhaInesperadaNoCalculoTambemNaoSalvaProjetoPreValidado() {
        ProjetoPreValidadoService projetoService = new ProjetoPreValidadoService();
        DemandaServiceObservavel demandaService = new DemandaServiceObservavel();
        demandaService.falha = new IllegalStateException("Falha inesperada no cálculo");
        ChecklistController controller = criarController(demandaService, projetoService);

        assertThrows(
                IllegalStateException.class,
                () -> controller.enviar(criarEdificacaoValida())
        );

        assertTrue(projetoService.listarTodos().isEmpty());
        assertTrue(demandaService.chamado);
    }
}

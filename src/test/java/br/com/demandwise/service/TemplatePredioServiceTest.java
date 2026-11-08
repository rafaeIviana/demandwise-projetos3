package br.com.demandwise.service;

import br.com.demandwise.model.Apartamento;
import br.com.demandwise.model.Edificacao;
import br.com.demandwise.projeto.TemplatePredio;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TemplatePredioServiceTest {

    private Edificacao edificacaoExemplo() {
        Edificacao e = new Edificacao(new ArrayList<>(List.of(
                new Apartamento(80, 10, 0, false),
                new Apartamento(100, 12, 0, false))));
        e.setPotenciaIluminacao(5);
        e.setPotenciaTomadas(7);
        e.setPotenciasMotores(new ArrayList<>(List.of(3.0)));
        return e;
    }

    @Test
    void salvaTemplateEPodeListarEBuscar() {
        TemplatePredioService service = new TemplatePredioService();
        TemplatePredio salvo = service.salvar("Prédio 2 aptos", edificacaoExemplo());

        assertEquals(1L, salvo.id());
        assertEquals(1, service.listarTodos().size());
        assertTrue(service.buscarPorId(1L).isPresent());
        assertTrue(service.buscarPorId(99L).isEmpty());
    }

    @Test
    void editarDadosCarregadosNaoAlteraOTemplateSalvo() {
        TemplatePredioService service = new TemplatePredioService();
        service.salvar("Modelo", edificacaoExemplo());

        Edificacao carregada = service.buscarPorId(1L).orElseThrow().edificacao();
        carregada.setPotenciaIluminacao(999);
        carregada.getApartamentos().get(0).setAreaUtil(999);
        carregada.getApartamentos().clear();

        Edificacao original = service.buscarPorId(1L).orElseThrow().edificacao();
        assertEquals(5, original.getPotenciaIluminacao());
        assertEquals(2, original.getApartamentos().size());
        assertEquals(80, original.getApartamentos().get(0).getAreaUtil());
    }

    @Test
    void alterarObjetoOriginalDepoisDeSalvarNaoAlteraOTemplate() {
        TemplatePredioService service = new TemplatePredioService();
        Edificacao e = edificacaoExemplo();
        service.salvar("Modelo", e);

        e.setPotenciaTomadas(1000);

        assertEquals(7, service.buscarPorId(1L).orElseThrow().edificacao().getPotenciaTomadas());
    }
}
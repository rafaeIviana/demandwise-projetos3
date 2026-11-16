package br.com.demandwise.service;

import br.com.demandwise.model.Apartamento;
import br.com.demandwise.model.Edificacao;
import br.com.demandwise.model.GrupoResidencial;
import br.com.demandwise.model.CargasCondominio;
import br.com.demandwise.model.CargaInstalada;
import br.com.demandwise.model.MotobombaIncendio;
import br.com.demandwise.model.Potencia;
import br.com.demandwise.model.UnidadePotencia;
import br.com.demandwise.projeto.TemplatePredio;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TemplatePredioServiceTest {

    @Test
    void preservaNovoModeloSemCompartilharAlteracoesDaEdificacao() {
        TemplatePredioService service = new TemplatePredioService();
        Edificacao origem = new Edificacao();
        var grupo = new GrupoResidencial("G1", "76–80 m²",
                BigDecimal.valueOf(76), BigDecimal.valueOf(80), 12);
        var potencia = new Potencia(BigDecimal.TEN, UnidadePotencia.KW);
        var cargas = new CargasCondominio(
                List.of(new CargaInstalada("Iluminação", potencia, 2)),
                List.of(), List.of(), List.of(), List.of(), List.of());
        var bomba = new MotobombaIncendio(true, "Bomba", potencia);
        origem.setGruposResidenciais(List.of(grupo));
        origem.setCargasCondominio(cargas);
        origem.setMotobombaIncendio(bomba);
        long id = service.salvar("Agrupado", origem).id();
        origem.setGruposResidenciais(List.of());
        origem.setCargasCondominio(null);
        origem.setMotobombaIncendio(null);

        Edificacao carregada = service.buscarPorId(id).orElseThrow().edificacao();
        assertEquals(List.of(grupo), carregada.getGruposResidenciais());
        assertEquals(cargas, carregada.getCargasCondominio());
        assertEquals(bomba, carregada.getMotobombaIncendio());
        carregada.setGruposResidenciais(List.of());
        carregada.setCargasCondominio(null);
        carregada.setMotobombaIncendio(null);

        Edificacao salva = service.buscarPorId(id).orElseThrow().edificacao();
        assertEquals(List.of(grupo), salva.getGruposResidenciais());
        assertEquals(cargas, salva.getCargasCondominio());
        assertEquals(bomba, salva.getMotobombaIncendio());
    }

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

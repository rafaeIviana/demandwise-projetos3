
package br.com.demandwise.service;

import br.com.demandwise.model.Apartamento;
import br.com.demandwise.model.Edificacao;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemandaServiceTest {

    @Test
    void deveRegistrarCriteriosNormativosNaMemoriaDeCalculo() {
        DemandaService service = new DemandaService();

        Edificacao edificacao = new Edificacao();

        Apartamento apartamento = new Apartamento();
        apartamento.setAreaUtil(40);
        apartamento.setPotenciaInstalada(10);

        edificacao.setApartamentos(List.of(apartamento));
        edificacao.setPotenciaIluminacao(10);
        edificacao.setPotenciaTomadas(10);

        ResultadoDemanda resultado = service.calcularDemanda(edificacao);

        assertTrue(resultado.memoriaCalculo().stream()
                .anyMatch(c -> c.criterio().equals("QUADRO_35")));

        assertTrue(resultado.memoriaCalculo().stream()
                .anyMatch(c -> c.criterio().equals("FATOR_COINCIDENCIA")));

        assertTrue(resultado.memoriaCalculo().stream()
                .anyMatch(c -> c.criterio().equals("FATOR_SEGURANCA")));
    }

    @Test
    void deveRegistrarValorCorretoDoQuadro35() {
        DemandaService service = new DemandaService();

        Edificacao edificacao = new Edificacao();

        Apartamento apartamento = new Apartamento();
        apartamento.setAreaUtil(40);
        apartamento.setPotenciaInstalada(10);

        edificacao.setApartamentos(List.of(apartamento));
        edificacao.setPotenciaIluminacao(10);
        edificacao.setPotenciaTomadas(10);

        ResultadoDemanda resultado = service.calcularDemanda(edificacao);

        CriterioAplicado criterioQuadro35 = resultado.memoriaCalculo().stream()
                .filter(c -> c.criterio().equals("QUADRO_35"))
                .findFirst()
                .orElseThrow();

        assertEquals(1.00, criterioQuadro35.valorAplicado(), 0.01);
    }

    @Test
    void deveRegistrarValorCorretoDoFatorDeCoincidencia() {
        DemandaService service = new DemandaService();

        Edificacao edificacao = new Edificacao();

        Apartamento apartamento = new Apartamento();
        apartamento.setAreaUtil(40);
        apartamento.setPotenciaInstalada(10);

        edificacao.setApartamentos(List.of(apartamento));
        edificacao.setPotenciaIluminacao(10);
        edificacao.setPotenciaTomadas(10);

        ResultadoDemanda resultado = service.calcularDemanda(edificacao);

        CriterioAplicado criterio = resultado.memoriaCalculo().stream()
                .filter(c -> c.criterio().equals("FATOR_COINCIDENCIA"))
                .findFirst()
                .orElseThrow();

        assertEquals(1.00, criterio.valorAplicado(), 0.01);
    }

    @Test
    void deveRegistrarValorCorretoDoFatorDeSeguranca() {
        DemandaService service = new DemandaService();

        Edificacao edificacao = new Edificacao();

        Apartamento apartamento = new Apartamento();
        apartamento.setAreaUtil(40);
        apartamento.setPotenciaInstalada(10);

        edificacao.setApartamentos(List.of(apartamento));
        edificacao.setPotenciaIluminacao(10);
        edificacao.setPotenciaTomadas(10);

        ResultadoDemanda resultado = service.calcularDemanda(edificacao);

        CriterioAplicado criterio = resultado.memoriaCalculo().stream()
                .filter(c -> c.criterio().equals("FATOR_SEGURANCA"))
                .findFirst()
                .orElseThrow();

        assertEquals(1.50, criterio.valorAplicado(), 0.01);
    }

    @Test
    void deveRegistrarCriterioDeIluminacaoETomadas() {
        DemandaService service = new DemandaService();

        Edificacao edificacao = new Edificacao();

        Apartamento apartamento = new Apartamento();
        apartamento.setAreaUtil(40);
        apartamento.setPotenciaInstalada(10);

        edificacao.setApartamentos(List.of(apartamento));
        edificacao.setPotenciaIluminacao(10);
        edificacao.setPotenciaTomadas(10);

        ResultadoDemanda resultado = service.calcularDemanda(edificacao);

        CriterioAplicado criterio = resultado.memoriaCalculo().stream()
                .filter(c -> c.criterio().equals("DEMANDA_ILUMINACAO_TOMADAS_PADRAO"))
                .findFirst()
                .orElseThrow();

        assertEquals(18.75, criterio.valorAplicado(), 0.01);
    }

    @Test
    void deveRegistrarDemandaDosMotores() {
        DemandaService service = new DemandaService();

        Edificacao edificacao = new Edificacao();

        Apartamento apartamento = new Apartamento();
        apartamento.setAreaUtil(40);
        apartamento.setPotenciaInstalada(10);

        edificacao.setApartamentos(List.of(apartamento));
        edificacao.setPotenciaIluminacao(10);
        edificacao.setPotenciaTomadas(10);
        edificacao.setPotenciasMotores(List.of(10.0, 4.0));

        ResultadoDemanda resultado = service.calcularDemanda(edificacao);

        CriterioAplicado criterio = resultado.memoriaCalculo().stream()
                .filter(c -> c.criterio().equals("DEMANDA_MOTORES"))
                .findFirst()
                .orElseThrow();

        assertEquals(12.00, criterio.valorAplicado(), 0.01);
    }

    @Test
    void deveRejeitarCalculoQuandoAreaUltrapassarLimiteDoQuadro35() {
        DemandaService service = new DemandaService();

        Edificacao edificacao = new Edificacao();

        Apartamento apartamento = new Apartamento();
        apartamento.setAreaUtil(1001);
        apartamento.setPotenciaInstalada(10);

        edificacao.setApartamentos(List.of(apartamento));
        edificacao.setPotenciaIluminacao(10);
        edificacao.setPotenciaTomadas(10);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.calcularDemanda(edificacao)
        );

        assertTrue(exception.getMessage().contains("1000 m²"));
    }
}

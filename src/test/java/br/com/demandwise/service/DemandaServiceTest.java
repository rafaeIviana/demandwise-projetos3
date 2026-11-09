
package br.com.demandwise.service;

import br.com.demandwise.model.Apartamento;
import br.com.demandwise.model.Edificacao;
import br.com.demandwise.model.GrupoResidencial;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
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

    @Test
    void deveSelecionarFrPelaDemandaDrMesmoQuandoDtrEstaEmOutraFaixa() {
        ResultadoDemanda resultado = calcularComApartamentos(26);

        assertEquals(0.81, valor(resultado, "FATOR_COINCIDENCIA"), 0.000001);
        assertEquals(1.3, valor(resultado, "FATOR_SEGURANCA"), 0.000001);
        assertTrue(resultado.memoriaCalculo().stream()
                .filter(c -> c.criterio().equals("FATOR_SEGURANCA"))
                .findFirst().orElseThrow().parametro().contains("demandaResidencialDr=26.0"));
    }

    @Test
    void deveUsarFaixaExplicitaDeFcParaSessentaOuMaisUnidades() {
        ResultadoDemanda resultado59 = calcularComApartamentos(59);
        ResultadoDemanda resultado60 = calcularComApartamentos(60);
        ResultadoDemanda resultado100 = calcularComApartamentos(100);

        assertEquals(0.6844, valor(resultado59, "FATOR_COINCIDENCIA"), 0.000001);
        assertEquals(0.6823, valor(resultado60, "FATOR_COINCIDENCIA"), 0.000001);
        assertEquals(0.6823, valor(resultado100, "FATOR_COINCIDENCIA"), 0.000001);
        assertTrue(resultado60.memoriaCalculo().stream()
                .filter(c -> c.criterio().equals("FATOR_COINCIDENCIA"))
                .findFirst().orElseThrow().descricao().contains("60 ou mais"));
    }

    @Test
    void deveRespeitarLimitesDasFaixasDeFrEmFuncaoDeDr() {
        assertEquals(1.5, valor(calcularComApartamentos(25), "FATOR_SEGURANCA"), 0.000001);
        assertEquals(1.3, valor(calcularComApartamentos(26), "FATOR_SEGURANCA"), 0.000001);
        assertEquals(1.3, valor(calcularComApartamentos(50), "FATOR_SEGURANCA"), 0.000001);
        assertEquals(1.2, valor(calcularComApartamentos(51), "FATOR_SEGURANCA"), 0.000001);
        assertEquals(1.2, valor(calcularComApartamentos(100), "FATOR_SEGURANCA"), 0.000001);
        assertEquals(1.1, valor(calcularComApartamentos(101), "FATOR_SEGURANCA"), 0.000001);
    }

    @Test
    void deveAplicarMudancaDeFaixaDoQuadro35NaAreaLimite() {
        assertEquals(1.47, demandaIndividualPorArea(65), 0.000001);
        assertEquals(1.57, demandaIndividualPorArea(65.01), 0.000001);
    }

    private ResultadoDemanda calcularComApartamentos(int quantidade) {
        Edificacao edificacao = new Edificacao();
        List<Apartamento> apartamentos = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            apartamentos.add(new Apartamento(40));
        }
        edificacao.setApartamentos(apartamentos);
        return new DemandaService().calcularDemanda(edificacao);
    }

    private double valor(ResultadoDemanda resultado, String criterio) {
        return resultado.memoriaCalculo().stream()
                .filter(c -> c.criterio().equals(criterio))
                .findFirst()
                .orElseThrow()
                .valorAplicado();
    }

    private double demandaIndividualPorArea(double area) {
        Edificacao edificacao = new Edificacao();
        edificacao.setGruposResidenciais(List.of(new GrupoResidencial(
                "limite", "Limite", BigDecimal.valueOf(area), BigDecimal.valueOf(area), 1)));
        return new DemandaService().calcularDemanda(edificacao)
                .detalhamento().residencial().demandaResidencialBaseDrKva().doubleValue();
    }
}

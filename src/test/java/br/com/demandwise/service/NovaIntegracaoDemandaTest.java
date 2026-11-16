package br.com.demandwise.service;

import br.com.demandwise.model.CargaInstalada;
import br.com.demandwise.model.CargasCondominio;
import br.com.demandwise.model.Edificacao;
import br.com.demandwise.model.GrupoResidencial;
import br.com.demandwise.model.MotobombaIncendio;
import br.com.demandwise.model.Potencia;
import br.com.demandwise.model.UnidadePotencia;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NovaIntegracaoDemandaTest {
    private final DemandaService demandaService = new DemandaService();

    @Test
    void calculaGruposResidenciaisComAsRegrasJaExistentesEExpoeResultadoDetalhado() {
        Edificacao edificacao = new Edificacao();
        edificacao.setGruposResidenciais(List.of(
                new GrupoResidencial("A", "Faixa A", new BigDecimal("61"), new BigDecimal("65"), 50),
                new GrupoResidencial("B", "Faixa B", new BigDecimal("66"), new BigDecimal("70"), 50)
        ));

        ResultadoDemanda resultado = demandaService.calcularDemanda(edificacao);
        ResultadoDemandaDetalhado detalhado = resultado.detalhamento();

        assertNotNull(detalhado);
        assertEquals(100, detalhado.residencial().numeroUnidades());
        assertEquals(new BigDecimal("152.0"), detalhado.residencial().demandaResidencialBaseDrKva());
        assertEquals(new BigDecimal("0.6823"), detalhado.residencial().fatorCoincidenciaFc());
        assertEquals(103.7096, detalhado.residencial().demandaCoincidenteDtrKva().doubleValue(), 0.00000001);
        assertEquals(new BigDecimal("1.1"), detalhado.residencial().fatorSegurancaFr());
        assertEquals(114.08056, detalhado.residencial().demandaResidencialFinalDrfKva().doubleValue(), 0.00000001);
        assertEquals(114.08, resultado.demandaTotalArredondada(), 0.00000001);
        assertEquals(detalhado.residencial().demandaResidencialFinalDrfKva().doubleValue(),
                resultado.demandaTotal(), 0.000001);
        assertTrue(criterio(resultado, "FATOR_SEGURANCA").parametro().contains("demandaResidencialDr=152.0"));
        assertTrue(detalhado.memoriaCalculo().stream().anyMatch(
                etapa -> etapa.etapa().equals("SUBTOTAL_GRUPO_RESIDENCIAL")));
        assertTrue(criterio(resultado, "FATOR_SEGURANCA").parametro().contains("demandaResidencialDr=152.0"));
    }

    @Test
    void mantemCalculoLegadoERejeitaMisturaAmbiguaDeRepresentacoesResidenciais() {
        Edificacao edificacao = new Edificacao();
        edificacao.setApartamentos(List.of(new br.com.demandwise.model.Apartamento(40)));
        edificacao.setGruposResidenciais(List.of(new GrupoResidencial(
                "A", "Faixa A", new BigDecimal("61"), new BigDecimal("65"), 2)));

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> demandaService.calcularDemanda(edificacao));

        assertTrue(erro.getMessage().contains("sem misturar"));
    }

    @Test
    void rejeitaGrupoQueAtravessaFaixasDoQuadroExistente() {
        Edificacao edificacao = new Edificacao();
        edificacao.setGruposResidenciais(List.of(new GrupoResidencial(
                "X", "Grupo X", new BigDecimal("65"), new BigDecimal("66"), 1)));

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> demandaService.calcularDemanda(edificacao));

        assertTrue(erro.getMessage().contains("atravessa mais de uma faixa"));
    }

    @Test
    void naoIgnoraCategoriasDeCondominioSemRegraConfirmada() {
        Edificacao edificacao = new Edificacao();
        edificacao.setGruposResidenciais(List.of(new GrupoResidencial(
                "A", "Faixa A", new BigDecimal("61"), new BigDecimal("65"), 1)));
        edificacao.setCargasCondominio(new CargasCondominio(
                List.of(), List.of(), List.of(),
                List.of(new CargaInstalada("Motor 1", new Potencia(new BigDecimal("12.5"), UnidadePotencia.CV), 1)),
                List.of(), List.of()));

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> demandaService.calcularDemanda(edificacao));

        assertTrue(erro.getMessage().contains("motores"));
        assertTrue(erro.getMessage().contains("regras normativas confirmadas"));
    }

    @Test
    void naoCalculaMotobombaSemRegraConfirmadaNemAceitaDemandaComoEntrada() {
        Edificacao edificacao = new Edificacao();
        edificacao.setGruposResidenciais(List.of(new GrupoResidencial(
                "A", "Faixa A", new BigDecimal("61"), new BigDecimal("65"), 1)));
        edificacao.setMotobombaIncendio(new MotobombaIncendio(
                true, "MBI", new Potencia(new BigDecimal("2"), UnidadePotencia.CV)));

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> demandaService.calcularDemanda(edificacao));

        assertTrue(erro.getMessage().contains("motobomba de incêndio"));
        assertFalse(java.util.Arrays.stream(MotobombaIncendio.class.getRecordComponents())
                .anyMatch(componente -> componente.getName().equalsIgnoreCase("dmbi")));
    }

    private CriterioAplicado criterio(ResultadoDemanda resultado, String nome) {
        return resultado.memoriaCalculo().stream()
                .filter(item -> item.criterio().equals(nome))
                .findFirst()
                .orElseThrow();
    }
}

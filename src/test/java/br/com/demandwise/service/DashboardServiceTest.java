package br.com.demandwise.service;

import br.com.demandwise.dto.DashboardResponse;
import br.com.demandwise.dto.MetricaMensal;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardServiceTest {

    private final DashboardService service = new DashboardService();

    @Test
    void deveExporSerieMensalComTaxasCoerentesComOsTotais() {
        DashboardResponse resposta = service.obterMetricas();

        assertTrue(resposta.dadosSimulados());
        assertEquals(12, resposta.meses().size());
        assertEquals("jan/26", resposta.meses().get(0).mes());
        assertEquals("dez/26", resposta.meses().get(11).mes());

        for (MetricaMensal mes : resposta.meses()) {
            double taxaEsperada = mes.projetosReprovados()
                    * 100.0 / mes.projetosAnalisados();
            double taxaArredondada = Math.round(taxaEsperada * 10.0) / 10.0;
            assertEquals(taxaArredondada, mes.taxaReprovacao(), 0.0, mes.mes());
        }
    }

    @Test
    void deveSepararOsPeriodosECalcularComparacaoPonderada() {
        DashboardResponse resposta = service.obterMetricas();
        List<MetricaMensal> antes = resposta.meses().stream()
                .filter(mes -> !mes.aposAdocao())
                .toList();
        List<MetricaMensal> depois = resposta.meses().stream()
                .filter(MetricaMensal::aposAdocao)
                .toList();

        assertEquals("jul/26", resposta.mesAdocao());
        assertEquals(6, antes.size());
        assertEquals(6, depois.size());
        assertEquals("jun/26", antes.get(antes.size() - 1).mes());
        assertEquals("jul/26", depois.get(0).mes());

        assertEquals(39.0, resposta.taxaMediaAntes(), 0.0);
        assertEquals(21.9, resposta.taxaMediaDepois(), 0.0);
        assertEquals(43.9, resposta.reducaoPercentual(), 0.0);
        assertTrue(resposta.taxaMediaAntes() > resposta.taxaMediaDepois());
        assertFalse(antes.get(0).aposAdocao());
        assertTrue(depois.get(0).aposAdocao());
    }
}

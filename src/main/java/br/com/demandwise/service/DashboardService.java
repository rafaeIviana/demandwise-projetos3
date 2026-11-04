package br.com.demandwise.service;

import br.com.demandwise.dto.DashboardResponse;
import br.com.demandwise.dto.MetricaMensal;
import org.springframework.stereotype.Service;

import java.util.List;

// US-10: os dados são simulados, só para demonstrar o conceito no protótipo
@Service
public class DashboardService {

    private static final String MES_ADOCAO = "jul/26";

    public DashboardResponse obterMetricas() {
        List<MetricaMensal> meses = List.of(
                mes("jan/26", 48, 18, false),
                mes("fev/26", 52, 21, false),
                mes("mar/26", 45, 17, false),
                mes("abr/26", 50, 20, false),
                mes("mai/26", 47, 19, false),
                mes("jun/26", 53, 20, false),
                mes("jul/26", 51, 16, true),
                mes("ago/26", 49, 13, true),
                mes("set/26", 55, 12, true),
                mes("out/26", 50, 10, true),
                mes("nov/26", 54, 9, true),
                mes("dez/26", 52, 8, true)
        );

        double taxaAntes = taxaDoPeriodo(meses, false);
        double taxaDepois = taxaDoPeriodo(meses, true);
        double reducao = taxaAntes == 0 ? 0 : (taxaAntes - taxaDepois) / taxaAntes * 100;

        return new DashboardResponse(true, MES_ADOCAO, meses,
                arredondar(taxaAntes), arredondar(taxaDepois), arredondar(reducao));
    }

    private MetricaMensal mes(String mes, int analisados, int reprovados, boolean aposAdocao) {
        double taxa = (double) reprovados / analisados * 100;
        return new MetricaMensal(mes, analisados, reprovados, arredondar(taxa), aposAdocao);
    }

    // taxa do período inteiro: total de reprovados dividido pelo total de analisados
    private double taxaDoPeriodo(List<MetricaMensal> meses, boolean aposAdocao) {
        int analisados = 0;
        int reprovados = 0;

        for (MetricaMensal m : meses) {
            if (m.aposAdocao() == aposAdocao) {
                analisados += m.projetosAnalisados();
                reprovados += m.projetosReprovados();
            }
        }

        return analisados == 0 ? 0 : (double) reprovados / analisados * 100;
    }

    private double arredondar(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }
}

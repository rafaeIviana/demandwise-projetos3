package br.com.demandwise.dto;

import java.util.List;

// resposta do dashboard de métricas de reprovação (US-10)
public record DashboardResponse(boolean dadosSimulados, String mesAdocao, List<MetricaMensal> meses,
                                double taxaMediaAntes, double taxaMediaDepois, double reducaoPercentual) {
}

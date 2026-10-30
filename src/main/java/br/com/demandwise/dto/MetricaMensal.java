package br.com.demandwise.dto;

// uma linha do dashboard (US-10): resultado de um mês
public record MetricaMensal(String mes, int projetosAnalisados, int projetosReprovados,
                            double taxaReprovacao, boolean aposAdocao) {
}

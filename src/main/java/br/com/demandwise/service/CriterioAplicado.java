package br.com.demandwise.service;

public record CriterioAplicado(
        String criterio,
        String parametro,
        String descricao,
        double valorAplicado
) {
}
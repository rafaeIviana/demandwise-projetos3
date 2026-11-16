package br.com.demandwise.service;

import java.util.List;

public record ResultadoDemanda(
        double demandaTotal,
        List<CriterioAplicado> memoriaCalculo,
        ResultadoDemandaDetalhado detalhamento
) {
    public ResultadoDemanda(double demandaTotal, List<CriterioAplicado> memoriaCalculo) {
        this(demandaTotal, memoriaCalculo, null);
    }

    public double demandaTotalArredondada() {
        return Math.round(demandaTotal * 100.0) / 100.0;
    }
}

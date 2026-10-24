package br.com.demandwise.service;

import java.util.List;

public record ResultadoDemanda(
        double demandaTotal,
        List<CriterioAplicado> memoriaCalculo
) {
    public double demandaTotalArredondada() {
        return Math.round(demandaTotal * 100.0) / 100.0;
    }
}

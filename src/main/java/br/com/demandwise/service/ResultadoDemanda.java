package br.com.demandwise.service;

import java.util.List;

public record ResultadoDemanda(
        double demandaTotal,
        List<CriterioAplicado> memoriaCalculo
) {
}
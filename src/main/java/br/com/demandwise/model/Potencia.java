package br.com.demandwise.model;

import java.math.BigDecimal;

/** A power value whose original unit is kept with the value. */
public record Potencia(BigDecimal valor, UnidadePotencia unidade) {
    public Potencia {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("A potência deve ser maior que zero.");
        }
        if (unidade == null) {
            throw new IllegalArgumentException("A unidade da potência é obrigatória.");
        }
    }
}

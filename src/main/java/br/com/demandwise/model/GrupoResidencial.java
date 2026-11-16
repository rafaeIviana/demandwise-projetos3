package br.com.demandwise.model;

import java.math.BigDecimal;

/** A residential typology grouping units that share an area band. Demand is derived later. */
public record GrupoResidencial(
        String identificacao,
        String tipologia,
        BigDecimal areaMinimaM2,
        BigDecimal areaMaximaM2,
        int quantidade
) {
    public GrupoResidencial {
        if (tipologia == null || tipologia.isBlank()) {
            throw new IllegalArgumentException("A tipologia residencial é obrigatória.");
        }
        if (areaMinimaM2 == null || areaMinimaM2.signum() <= 0
                || areaMaximaM2 == null || areaMaximaM2.signum() <= 0) {
            throw new IllegalArgumentException("A faixa de área deve ser maior que zero.");
        }
        if (areaMinimaM2.compareTo(areaMaximaM2) > 0) {
            throw new IllegalArgumentException("A área mínima não pode exceder a área máxima.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade de unidades deve ser maior que zero.");
        }
    }
}

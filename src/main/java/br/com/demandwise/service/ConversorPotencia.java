package br.com.demandwise.service;

import br.com.demandwise.model.Potencia;
import br.com.demandwise.model.UnidadePotencia;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Converts only direct W/kW unit equivalents; it does not convert real to apparent power. */
@Service
public class ConversorPotencia {
    private static final BigDecimal MIL = BigDecimal.valueOf(1000);

    public Potencia converter(Potencia potencia, UnidadePotencia unidadeDestino) {
        if (potencia == null || unidadeDestino == null) {
            throw new IllegalArgumentException("Potência e unidade de destino são obrigatórias.");
        }
        if (potencia.unidade() == unidadeDestino) {
            return potencia;
        }

        BigDecimal valorConvertido;
        if (potencia.unidade() == UnidadePotencia.W && unidadeDestino == UnidadePotencia.KW) {
            valorConvertido = potencia.valor().divide(MIL);
        } else if (potencia.unidade() == UnidadePotencia.KW && unidadeDestino == UnidadePotencia.W) {
            valorConvertido = potencia.valor().multiply(MIL);
        } else {
            throw new IllegalArgumentException("Conversão de " + potencia.unidade() + " para "
                    + unidadeDestino + " não está definida nesta fase.");
        }

        return new Potencia(valorConvertido, unidadeDestino);
    }
}

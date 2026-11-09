package br.com.demandwise.service;

import br.com.demandwise.model.Potencia;
import br.com.demandwise.model.UnidadePotencia;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ConversorPotenciaTest {
    private final ConversorPotencia conversor = new ConversorPotencia();

    @Test
    void converteWattEQuilowattSemAlterarEntradaOriginal() {
        Potencia original = new Potencia(new BigDecimal("12500"), UnidadePotencia.W);

        Potencia convertida = conversor.converter(original, UnidadePotencia.KW);

        assertEquals(0, new BigDecimal("12.5").compareTo(convertida.valor()));
        assertEquals(UnidadePotencia.KW, convertida.unidade());
        assertEquals(new BigDecimal("12500"), original.valor());
        assertEquals(UnidadePotencia.W, original.unidade());
    }

    @Test
    void converteQuilowattParaWattEDeixaUnidadeIgualSemAlterarValor() {
        Potencia original = new Potencia(new BigDecimal("2.75"), UnidadePotencia.KW);

        assertEquals(0, new BigDecimal("2750").compareTo(
                conversor.converter(original, UnidadePotencia.W).valor()));
        assertSame(original, conversor.converter(original, UnidadePotencia.KW));
    }

    @Test
    void naoConverteEntreUnidadesQueExigemRegraAdicional() {
        Potencia motor = new Potencia(new BigDecimal("12.5"), UnidadePotencia.CV);
        Potencia potenciaAparente = new Potencia(new BigDecimal("10"), UnidadePotencia.KVA);

        assertThrows(IllegalArgumentException.class,
                () -> conversor.converter(motor, UnidadePotencia.KVA));
        assertThrows(IllegalArgumentException.class,
                () -> conversor.converter(potenciaAparente, UnidadePotencia.KW));
    }
}

package br.com.demandwise.projeto;

import br.com.demandwise.model.Edificacao;

public record TemplatePredio(
        Long id,
        String nome,
        Edificacao edificacao
) {
}
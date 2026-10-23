
package br.com.demandwise.projeto;

import br.com.demandwise.model.Edificacao;
import br.com.demandwise.service.ResultadoDemanda;

public record ProjetoPreValidado(
        Long id,
        String status,
        Edificacao edificacao,
        ResultadoDemanda resultadoDemanda
) {
}

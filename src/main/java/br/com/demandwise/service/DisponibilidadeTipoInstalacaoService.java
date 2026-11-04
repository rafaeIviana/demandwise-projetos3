package br.com.demandwise.service;

import br.com.demandwise.model.Edificacao;
import br.com.demandwise.model.TipoInstalacao;
import org.springframework.stereotype.Service;

@Service
public class DisponibilidadeTipoInstalacaoService {

    public String mensagemIndisponibilidade(Edificacao edificacao) {
        if (edificacao == null) {
            return "Edificação não informada";
        }

        TipoInstalacao tipo = edificacao.getTipoInstalacao();
        if (tipo == null) {
            return "Tipo de instalação não informado";
        }
        if (tipo.isDisponivel()) {
            return null;
        }

        return "O cálculo para instalação do tipo " + tipo.getDescricao()
                + " estará disponível em breve. Nesta fase, apenas o tipo Residencial está implementado.";
    }
}

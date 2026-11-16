package br.com.demandwise.service;

import br.com.demandwise.model.CargasCondominio;
import br.com.demandwise.model.CargaInstalada;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Prevents new condominium categories from being silently omitted before their rules are confirmed. */
@Service
public class DemandaServicoService {
    public void validarRegrasDisponiveis(CargasCondominio cargas) {
        if (cargas == null) return;

        List<String> pendentes = new ArrayList<>();
        adicionarSeInformada(pendentes, "iluminação", cargas.iluminacao());
        adicionarSeInformada(pendentes, "tomadas de uso geral", cargas.tomadasUsoGeral());
        adicionarSeInformada(pendentes, "ar-condicionado", cargas.arCondicionado());
        adicionarSeInformada(pendentes, "motores", cargas.motores());
        adicionarSeInformada(pendentes, "equipamentos especiais", cargas.equipamentosEspeciais());
        adicionarSeInformada(pendentes, "bombas/hidromassagem", cargas.bombasHidromassagem());

        if (!pendentes.isEmpty()) {
            throw new IllegalArgumentException("Não foi calculada a demanda: ainda faltam regras normativas "
                    + "confirmadas para as categorias " + String.join(", ", pendentes) + ".");
        }
    }

    private void adicionarSeInformada(List<String> pendentes, String nome, List<CargaInstalada> cargas) {
        if (cargas != null && !cargas.isEmpty()) pendentes.add(nome);
    }
}

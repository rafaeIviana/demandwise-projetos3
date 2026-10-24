package br.com.demandwise.service;

import br.com.demandwise.dto.ChecklistResponse;
import br.com.demandwise.model.Apartamento;
import br.com.demandwise.model.Edificacao;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChecklistService {

    public ChecklistResponse verificar(Edificacao edificacao) {
        List<String> camposObrigatorios = new ArrayList<>();
        List<String> pendencias = new ArrayList<>();

        camposObrigatorios.add("Apartamentos da edificação");
        List<Apartamento> apartamentos = edificacao.getApartamentos();

        if (apartamentos == null || apartamentos.isEmpty()) {
            pendencias.add("Nenhum apartamento foi informado");
        } else {
            for (int i = 0; i < apartamentos.size(); i++) {
                Apartamento apartamento = apartamentos.get(i);
                int numero = i + 1;

                camposObrigatorios.add("Apartamento " + numero + " - área útil");
                if (!valorPositivoFinito(apartamento.getAreaUtil())) {
                    pendencias.add("Apartamento " + numero + ": área útil não informada");
                }

                camposObrigatorios.add("Apartamento " + numero + " - potência instalada");
                if (!valorPositivoFinito(apartamento.getPotenciaInstalada())) {
                    pendencias.add("Apartamento " + numero + ": potência instalada não informada");
                }
            }
        }

        camposObrigatorios.add("Potência de iluminação");
        if (!valorPositivoFinito(edificacao.getPotenciaIluminacao())) {
            pendencias.add("Potência de iluminação não informada");
        }

        camposObrigatorios.add("Potência de tomadas");
        if (!valorPositivoFinito(edificacao.getPotenciaTomadas())) {
            pendencias.add("Potência de tomadas não informada");
        }

        return new ChecklistResponse(pendencias.isEmpty(), camposObrigatorios, pendencias);
    }

    private boolean valorPositivoFinito(double valor) {
        return Double.isFinite(valor) && valor > 0;
    }
}

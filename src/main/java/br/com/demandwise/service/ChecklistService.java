package br.com.demandwise.service;

import br.com.demandwise.dto.ChecklistResponse;
import br.com.demandwise.model.Apartamento;
import br.com.demandwise.model.Edificacao;
import br.com.demandwise.model.GrupoResidencial;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChecklistService {

    public ChecklistResponse verificar(Edificacao edificacao) {
        List<String> camposObrigatorios = new ArrayList<>();
        List<String> pendencias = new ArrayList<>();

        if (edificacao == null) {
            camposObrigatorios.add("Edificação");
            pendencias.add("Edificação não informada");
            return new ChecklistResponse(false, camposObrigatorios, pendencias);
        }

        List<Apartamento> apartamentos = edificacao.getApartamentos();
        List<GrupoResidencial> grupos = edificacao.getGruposResidenciais();
        boolean possuiApartamentos = apartamentos != null && !apartamentos.isEmpty();
        boolean possuiGrupos = grupos != null && !grupos.isEmpty();

        if (possuiApartamentos && possuiGrupos) {
            camposObrigatorios.add("Apartamentos ou grupos residenciais");
            pendencias.add("Informe apartamentos individuais ou grupos residenciais, sem misturar as representações");
        } else if (possuiGrupos) {
            camposObrigatorios.add("Grupos residenciais");
            for (int i = 0; i < grupos.size(); i++) {
                GrupoResidencial grupo = grupos.get(i);
                int numero = i + 1;
                if (grupo == null) {
                    pendencias.add("Grupo residencial " + numero + " não informado");
                    continue;
                }
                camposObrigatorios.add("Grupo residencial " + numero + " - faixa de área e quantidade");
                if (grupo.tipologia() == null || grupo.tipologia().isBlank()
                        || grupo.areaMinimaM2() == null || grupo.areaMinimaM2().signum() <= 0
                        || grupo.areaMaximaM2() == null || grupo.areaMaximaM2().signum() <= 0
                        || grupo.areaMinimaM2().compareTo(grupo.areaMaximaM2()) > 0
                        || grupo.areaMaximaM2().doubleValue() > 1000) {
                    pendencias.add("Grupo residencial " + numero + ": faixa de área inválida para o Quadro 35");
                }
                if (grupo.quantidade() <= 0) {
                    pendencias.add("Grupo residencial " + numero + ": quantidade deve ser maior que zero");
                }
            }

            if (edificacao.getPotenciaIluminacao() != 0 || edificacao.getPotenciaTomadas() != 0
                    || (edificacao.getPotenciasMotores() != null && !edificacao.getPotenciasMotores().isEmpty())) {
                pendencias.add("O envio por grupos residenciais não aceita cargas legadas de serviço nesta etapa");
            }
        } else if (possuiApartamentos) {
            camposObrigatorios.add("Apartamentos da edificação");
            for (int i = 0; i < apartamentos.size(); i++) {
                Apartamento apartamento = apartamentos.get(i);
                int numero = i + 1;

                if (apartamento == null) {
                    camposObrigatorios.add("Apartamento " + numero);
                    pendencias.add("Apartamento " + numero + " não informado");
                    continue;
                }

                camposObrigatorios.add("Apartamento " + numero + " - área útil");
                if (!valorPositivoFinito(apartamento.getAreaUtil())) {
                    pendencias.add("Apartamento " + numero + ": área útil não informada");
                }

                camposObrigatorios.add("Apartamento " + numero + " - potência instalada");
                if (!valorPositivoFinito(apartamento.getPotenciaInstalada())) {
                    pendencias.add("Apartamento " + numero + ": potência instalada não informada");
                }
            }
        } else {
            camposObrigatorios.add("Apartamentos ou grupos residenciais");
            pendencias.add("Nenhum apartamento foi informado");
        }

        if (!possuiGrupos) {
            camposObrigatorios.add("Potência de iluminação");
            if (!valorPositivoFinito(edificacao.getPotenciaIluminacao())) {
                pendencias.add("Potência de iluminação não informada");
            }

            camposObrigatorios.add("Potência de tomadas");
            if (!valorPositivoFinito(edificacao.getPotenciaTomadas())) {
                pendencias.add("Potência de tomadas não informada");
            }
        }

        return new ChecklistResponse(pendencias.isEmpty(), camposObrigatorios, pendencias);
    }

    private boolean valorPositivoFinito(double valor) {
        return Double.isFinite(valor) && valor > 0;
    }
}

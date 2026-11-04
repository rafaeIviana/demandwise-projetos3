package br.com.demandwise.dto;

import br.com.demandwise.service.ResultadoDemanda;
import java.util.List;

public record ChecklistResponse(
        boolean liberadoParaEnvio,
        List<String> camposObrigatorios,
        List<String> pendencias,
        ResultadoDemanda resultadoDemanda) {

    public ChecklistResponse(
            boolean liberadoParaEnvio,
            List<String> camposObrigatorios,
            List<String> pendencias) {
        this(liberadoParaEnvio, camposObrigatorios, pendencias, null);
    }
}

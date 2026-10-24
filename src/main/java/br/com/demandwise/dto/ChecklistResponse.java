package br.com.demandwise.dto;

import java.util.List;
public record ChecklistResponse(boolean liberadoParaEnvio, List<String> camposObrigatorios, List<String> pendencias) {
}

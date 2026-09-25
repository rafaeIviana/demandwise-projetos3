package br.com.demandwise.dto;

import java.util.List;

// resposta do checklist de pendencias MUC (US-03)
public record ChecklistResponse(boolean liberadoParaEnvio, List<String> camposObrigatorios, List<String> pendencias) {
}

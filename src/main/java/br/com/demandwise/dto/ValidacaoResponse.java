package br.com.demandwise.dto;

import java.util.List;

/**
 * Resposta da validação em tempo real (US-02).
 * "valido" indica se o dado passou em todas as regras;
 * "alertas" traz as mensagens explicando o motivo de cada violação,
 * já referenciando a norma correspondente.
 */
public record ValidacaoResponse(boolean valido, List<String> alertas) {
}

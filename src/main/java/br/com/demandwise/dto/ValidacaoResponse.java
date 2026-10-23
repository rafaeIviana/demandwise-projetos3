package br.com.demandwise.dto;

import java.util.List;


public record ValidacaoResponse(boolean valido, List<String> alertas) {
}

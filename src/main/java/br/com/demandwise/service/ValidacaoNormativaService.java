package br.com.demandwise.service;

import br.com.demandwise.dto.ValidacaoResponse;
import br.com.demandwise.model.Apartamento;
import br.com.demandwise.model.Edificacao;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ValidacaoNormativaService {

    private final Validator validator;

    public ValidacaoNormativaService(Validator validator) {
        this.validator = validator;
    }

    public ValidacaoResponse validarApartamento(Apartamento apartamento) {
        if (apartamento == null) {
            return new ValidacaoResponse(false, List.of("Apartamento não informado"));
        }
        return validar(apartamento);
    }

    public ValidacaoResponse validarEdificacao(Edificacao edificacao) {
        if (edificacao == null) {
            return new ValidacaoResponse(false, List.of("Edificação não informada"));
        }
        return validar(edificacao);
    }

    private <T> ValidacaoResponse validar(T objeto) {
        List<String> alertas = validator.validate(objeto).stream()
                .map(ConstraintViolation::getMessage)
                .toList();
        return new ValidacaoResponse(alertas.isEmpty(), alertas);
    }
}

package br.com.demandwise.controller;

import br.com.demandwise.dto.ValidacaoResponse;
import br.com.demandwise.model.Apartamento;
import br.com.demandwise.model.Edificacao;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/validacao")
public class ValidacaoController {

    @PostMapping("/apartamento")
    public ValidacaoResponse validarApartamento(@Valid @RequestBody Apartamento apartamento,
                                                 BindingResult resultado) {
        return montarResposta(resultado);
    }

    @PostMapping("/edificacao")
    public ValidacaoResponse validarEdificacao(@Valid @RequestBody Edificacao edificacao,
                                                BindingResult resultado) {
        return montarResposta(resultado);
    }

    private ValidacaoResponse montarResposta(BindingResult resultado) {
        List<String> alertas = resultado.getAllErrors().stream()
                .map(erro -> erro.getDefaultMessage())
                .collect(Collectors.toList());
        return new ValidacaoResponse(alertas.isEmpty(), alertas);
    }
}

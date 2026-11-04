package br.com.demandwise.controller;

import br.com.demandwise.dto.ValidacaoResponse;
import br.com.demandwise.model.Apartamento;
import br.com.demandwise.model.Edificacao;
import br.com.demandwise.service.ValidacaoNormativaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/validacao")
public class ValidacaoController {

    private final ValidacaoNormativaService validacaoNormativaService;

    public ValidacaoController(ValidacaoNormativaService validacaoNormativaService) {
        this.validacaoNormativaService = validacaoNormativaService;
    }

    @PostMapping("/apartamento")
    public ValidacaoResponse validarApartamento(@RequestBody Apartamento apartamento) {
        return validacaoNormativaService.validarApartamento(apartamento);
    }

    @PostMapping("/edificacao")
    public ValidacaoResponse validarEdificacao(@RequestBody Edificacao edificacao) {
        return validacaoNormativaService.validarEdificacao(edificacao);
    }
}

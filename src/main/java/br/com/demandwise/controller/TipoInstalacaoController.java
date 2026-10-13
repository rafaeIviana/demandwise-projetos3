package br.com.demandwise.controller;

import br.com.demandwise.dto.TipoInstalacaoResponse;
import br.com.demandwise.model.TipoInstalacao;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/tipos-instalacao")
public class TipoInstalacaoController {

    @GetMapping
    public List<TipoInstalacaoResponse> listar() {
        return Arrays.stream(TipoInstalacao.values())
                .map(tipo -> new TipoInstalacaoResponse(
                        tipo.name(),
                        tipo.getDescricao(),
                        tipo.isDisponivel(),
                        tipo.isDisponivel() ? "Disponível" : "Em breve"))
                .toList();
    }
}
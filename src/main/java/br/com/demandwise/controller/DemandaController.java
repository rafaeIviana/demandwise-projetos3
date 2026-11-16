
package br.com.demandwise.controller;

import br.com.demandwise.model.Edificacao;
import br.com.demandwise.service.DemandaService;
import br.com.demandwise.service.DisponibilidadeTipoInstalacaoService;
import br.com.demandwise.service.ResultadoDemanda;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/demanda")
public class DemandaController {

    private final DemandaService demandaService;
    private final DisponibilidadeTipoInstalacaoService disponibilidadeTipoInstalacaoService;

    public DemandaController(
            DemandaService demandaService,
            DisponibilidadeTipoInstalacaoService disponibilidadeTipoInstalacaoService) {
        this.demandaService = demandaService;
        this.disponibilidadeTipoInstalacaoService = disponibilidadeTipoInstalacaoService;
    }

    @PostMapping("/calcular")
    public ResponseEntity<?> calcular(@RequestBody Edificacao edificacao) {
        String indisponibilidade =
                disponibilidadeTipoInstalacaoService.mensagemIndisponibilidade(edificacao);
        if (indisponibilidade != null) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of(
                    "mensagem", indisponibilidade
            ));
        }

        try {
            ResultadoDemanda resultado =
                    demandaService.calcularDemanda(edificacao);

            return ResponseEntity.ok(
                    new ResultadoDemanda(
                            resultado.demandaTotalArredondada(),
                            resultado.memoriaCalculo(),
                            resultado.detalhamento()
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "mensagem", e.getMessage()
            ));
        }
    }
}

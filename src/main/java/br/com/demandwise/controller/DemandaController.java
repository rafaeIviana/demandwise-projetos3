package br.com.demandwise.controller;

import br.com.demandwise.model.Edificacao;
import br.com.demandwise.model.TipoInstalacao;
import br.com.demandwise.service.DemandaService;
import br.com.demandwise.service.ResultadoDemanda;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/demanda")
public class DemandaController {

    private final DemandaService demandaService;

    public DemandaController(DemandaService demandaService) {
        this.demandaService = demandaService;
    }

    @PostMapping("/calcular")
    public ResponseEntity<?> calcular(@RequestBody Edificacao edificacao) {
        TipoInstalacao tipo = edificacao.getTipoInstalacao();

        // tipos ainda nao implementados respondem "em breve" em vez de calcular com regra errada
        if (!tipo.isDisponivel()) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of(
                    "mensagem", "O cálculo para instalação do tipo " + tipo.getDescricao()
                            + " estará disponível em breve. Nesta fase, apenas o tipo Residencial está implementado."));
        }

        ResultadoDemanda resultado = demandaService.calcularDemanda(edificacao);

        double demandaArredondada = Math.round(resultado.demandaTotal() * 100.0) / 100.0;

        return ResponseEntity.ok(new ResultadoDemanda(demandaArredondada, resultado.memoriaCalculo()));
    }
}
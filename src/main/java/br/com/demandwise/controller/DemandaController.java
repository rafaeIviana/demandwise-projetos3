package br.com.demandwise.controller;

import br.com.demandwise.model.Edificacao;
import br.com.demandwise.service.DemandaService;
import br.com.demandwise.service.ResultadoDemanda;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demanda")
public class DemandaController {

    private final DemandaService demandaService;

    public DemandaController(DemandaService demandaService) {
        this.demandaService = demandaService;
    }

    @PostMapping("/calcular")
    public ResultadoDemanda calcular(@RequestBody Edificacao edificacao) {
         ResultadoDemanda resultado = demandaService.calcularDemanda(edificacao);

        double demandaArredondada = Math.round(resultado.demandaTotal() * 100.0) / 100.0;

        return new ResultadoDemanda(demandaArredondada, resultado.memoriaCalculo());
    }
}
package br.com.demandwise.controller;

import br.com.demandwise.model.Edificacao;
import br.com.demandwise.service.DemandaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demanda")
public class DemandaController {

    private final DemandaService demandaService;

    public DemandaController(DemandaService demandaService) {
        this.demandaService = demandaService;
    }

    @PostMapping("/calcular")
    public double calcular(@RequestBody Edificacao edificacao) {
        double demanda = demandaService.calcularDemanda(edificacao);
    
        return Math.round(demanda * 100.0) / 100.0;
    }
}
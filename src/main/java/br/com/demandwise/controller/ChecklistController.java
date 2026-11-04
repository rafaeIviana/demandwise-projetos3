
package br.com.demandwise.controller;

import br.com.demandwise.dto.ChecklistResponse;
import br.com.demandwise.model.Edificacao;
import br.com.demandwise.projeto.ProjetoPreValidado;
import br.com.demandwise.service.ChecklistService;
import br.com.demandwise.service.DemandaService;
import br.com.demandwise.service.ProjetoPreValidadoService;
import br.com.demandwise.service.ResultadoDemanda;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checklist")
public class ChecklistController {

    private final ChecklistService checklistService;
    private final DemandaService demandaService;
    private final ProjetoPreValidadoService projetoPreValidadoService;

    public ChecklistController(
            ChecklistService checklistService,
            DemandaService demandaService,
            ProjetoPreValidadoService projetoPreValidadoService) {
        this.checklistService = checklistService;
        this.demandaService = demandaService;
        this.projetoPreValidadoService = projetoPreValidadoService;
    }

    @PostMapping("/verificar")
    public ChecklistResponse verificar(@RequestBody Edificacao edificacao) {
        return checklistService.verificar(edificacao);
    }

    @PostMapping("/enviar")
    public ResponseEntity<ChecklistResponse> enviar(
            @RequestBody Edificacao edificacao) {

        ChecklistResponse checklist = checklistService.verificar(edificacao);

        if (!checklist.liberadoParaEnvio()) {
            return ResponseEntity.badRequest().body(checklist);
        }

        ResultadoDemanda resultado =
                demandaService.calcularDemanda(edificacao);

        ProjetoPreValidado projeto = new ProjetoPreValidado(
                null,
                "Pré-validado",
                edificacao,
                resultado
        );

        projetoPreValidadoService.salvar(projeto);

        return ResponseEntity.ok(checklist);
    }
}

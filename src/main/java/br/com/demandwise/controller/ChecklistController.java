package br.com.demandwise.controller;

import br.com.demandwise.dto.ChecklistResponse;
import br.com.demandwise.model.Edificacao;
import br.com.demandwise.service.ChecklistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checklist")
public class ChecklistController {

    private final ChecklistService checklistService;

    public ChecklistController(ChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    @PostMapping("/verificar")
    public ChecklistResponse verificar(@RequestBody Edificacao edificacao) {
        return checklistService.verificar(edificacao);
    }

    @PostMapping("/enviar")
    public ResponseEntity<ChecklistResponse> enviar(@RequestBody Edificacao edificacao) {
        ChecklistResponse checklist = checklistService.verificar(edificacao);

        if (!checklist.liberadoParaEnvio()) {
            return ResponseEntity.badRequest().body(checklist);
        }

        return ResponseEntity.ok(checklist);
    }
}

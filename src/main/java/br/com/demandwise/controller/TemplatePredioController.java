package br.com.demandwise.controller;

import br.com.demandwise.model.Edificacao;
import br.com.demandwise.projeto.TemplatePredio;
import br.com.demandwise.service.TemplatePredioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
public class TemplatePredioController {

    public record SalvarTemplateRequest(String nome, Edificacao edificacao) {
    }

    private final TemplatePredioService service;

    public TemplatePredioController(TemplatePredioService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> salvar(@RequestBody SalvarTemplateRequest request) {
        if (request.nome() == null || request.nome().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "mensagem", "Informe um nome para o template."));
        }
        if (request.edificacao() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "mensagem", "Os dados do projeto são obrigatórios."));
        }
        TemplatePredio salvo = service.salvar(request.nome(), request.edificacao());
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @GetMapping
    public List<TemplatePredio> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        return service.buscarPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "mensagem", "Template não encontrado.")));
    }
}
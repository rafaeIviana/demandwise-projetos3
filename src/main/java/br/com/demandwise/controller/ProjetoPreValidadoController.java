
package br.com.demandwise.controller;

import br.com.demandwise.projeto.ProjetoPreValidado;
import br.com.demandwise.service.ProjetoPreValidadoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projetos-pre-validados")
public class ProjetoPreValidadoController {

    private final ProjetoPreValidadoService projetoPreValidadoService;

    public ProjetoPreValidadoController(
            ProjetoPreValidadoService projetoPreValidadoService) {
        this.projetoPreValidadoService = projetoPreValidadoService;
    }

    @GetMapping
    public List<ProjetoPreValidado> listarTodos() {
        return projetoPreValidadoService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjetoPreValidado> buscarPorId(
            @PathVariable Long id) {

        ProjetoPreValidado projeto =
                projetoPreValidadoService.buscarPorId(id);

        if (projeto == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(projeto);
    }
}

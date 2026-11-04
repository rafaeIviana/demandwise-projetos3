package br.com.demandwise.controller;

import br.com.demandwise.projeto.ProjetoPreValidado;
import br.com.demandwise.service.ProjetoPreValidadoService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/analista/projetos")
public class PainelAnalistaController {

    private final ProjetoPreValidadoService projetoPreValidadoService;

    public PainelAnalistaController(ProjetoPreValidadoService projetoPreValidadoService) {
        this.projetoPreValidadoService = projetoPreValidadoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("projetos", projetoPreValidadoService.listarTodos());
        return "analista/projetos";
    }

    @GetMapping("/{id}")
    public ModelAndView detalhes(@PathVariable Long id) {
        ProjetoPreValidado projeto = projetoPreValidadoService.buscarPorId(id);
        if (projeto == null) {
            ModelAndView naoEncontrado = new ModelAndView("analista/projeto-nao-encontrado");
            naoEncontrado.setStatus(HttpStatus.NOT_FOUND);
            naoEncontrado.addObject("id", id);
            return naoEncontrado;
        }

        ModelAndView detalhes = new ModelAndView("analista/projeto-detalhes");
        detalhes.addObject("projeto", projeto);
        return detalhes;
    }
}

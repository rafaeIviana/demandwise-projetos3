package br.com.demandwise.controller;

import br.com.demandwise.security.AutenticacaoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AutenticacaoController {
    private final AutenticacaoService autenticacaoService;
    public AutenticacaoController(AutenticacaoService autenticacaoService) { this.autenticacaoService = autenticacaoService; }
    @GetMapping("/login") public String login() { return "login"; }
    @PostMapping("/login") public String autenticar(@RequestParam String usuario, @RequestParam String senha, HttpSession session, Model model) {
        if (!autenticacaoService.autenticar(usuario, senha)) { model.addAttribute("erro", "Usuário ou senha inválidos."); return "login"; }
        session.setAttribute("autenticado", true);
        return "redirect:/envio";
    }
    @PostMapping("/logout") public String logout(HttpSession session) { session.invalidate(); return "redirect:/login"; }
}

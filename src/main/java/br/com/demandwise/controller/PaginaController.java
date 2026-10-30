package br.com.demandwise.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class PaginaController {

    @GetMapping("/envio")
    public String telaDeEnvio() {
        return "envio";
    }

    @GetMapping("/dashboard")
    public String telaDeDashboard() {
        return "dashboard";
    }
}

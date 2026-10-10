package br.com.demandwise.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// serve a tela de envio do projeto (US-06). As outras controllers são só API (JSON)
@Controller
public class PaginaController {

    @GetMapping("/envio")
    public String telaDeEnvio() {
        // devolve o arquivo templates/envio.html
        return "envio";
    }
}

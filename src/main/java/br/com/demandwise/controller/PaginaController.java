
package br.com.demandwise.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaginaController {

    @GetMapping("/")
    public String paginaInicial() {
        return "envio";
    }

    @GetMapping("/envio")
    public String paginaEnvio() {
        return "envio";
    }
}

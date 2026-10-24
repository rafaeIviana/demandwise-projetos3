package br.com.demandwise.controller;

import br.com.demandwise.dto.ChecklistResponse;
import br.com.demandwise.model.Edificacao;
import br.com.demandwise.model.TipoInstalacao;
import br.com.demandwise.service.ChecklistService;
import br.com.demandwise.service.DemandaService;
import br.com.demandwise.service.RelatorioService;
import br.com.demandwise.service.ResultadoDemanda;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/relatorio")
public class RelatorioController {

    private static final Logger logger = LoggerFactory.getLogger(RelatorioController.class);

    private final ChecklistService checklistService;
    private final DemandaService demandaService;
    private final RelatorioService relatorioService;

    public RelatorioController(
            ChecklistService checklistService,
            DemandaService demandaService,
            RelatorioService relatorioService
    ) {
        this.checklistService = checklistService;
        this.demandaService = demandaService;
        this.relatorioService = relatorioService;
    }

    @PostMapping("/pdf")
    public ResponseEntity<?> gerarPdf(@RequestBody(required = false) Edificacao edificacao) {
        if (edificacao == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "mensagem", "Os dados da edificação são obrigatórios."
            ));
        }

        TipoInstalacao tipo = edificacao.getTipoInstalacao();
        if (tipo == null || !tipo.isDisponivel()) {
            String descricao = tipo == null ? "informado" : tipo.getDescricao();
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of(
                    "mensagem", "O relatório para o tipo " + descricao + " estará disponível em breve."
            ));
        }

        ChecklistResponse checklist = checklistService.verificar(edificacao);
        if (!checklist.liberadoParaEnvio()) {
            return ResponseEntity.badRequest().body(checklist);
        }

        ResultadoDemanda resultado;
        try {
            resultado = demandaService.calcularDemanda(edificacao);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", e.getMessage()));
        }

        try {
            byte[] pdf = relatorioService.gerarPdf(edificacao, resultado);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header("Content-Disposition", "attachment; filename=\"relatorio-calculo-demanda.pdf\"")
                    .body(pdf);
        } catch (RuntimeException e) {
            logger.error("Falha ao gerar o relatório PDF.", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "mensagem", "Não foi possível gerar o relatório PDF. Tente novamente."
            ));
        }
    }
}

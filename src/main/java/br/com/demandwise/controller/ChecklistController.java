
package br.com.demandwise.controller;

import br.com.demandwise.dto.ChecklistResponse;
import br.com.demandwise.dto.ValidacaoResponse;
import br.com.demandwise.model.Edificacao;
import br.com.demandwise.projeto.ProjetoPreValidado;
import br.com.demandwise.service.ChecklistService;
import br.com.demandwise.service.DemandaService;
import br.com.demandwise.service.DisponibilidadeTipoInstalacaoService;
import br.com.demandwise.service.ProjetoPreValidadoService;
import br.com.demandwise.service.ResultadoDemanda;
import br.com.demandwise.service.ValidacaoNormativaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@RestController
@RequestMapping("/api/checklist")
public class ChecklistController {

    private final ChecklistService checklistService;
    private final ValidacaoNormativaService validacaoNormativaService;
    private final DisponibilidadeTipoInstalacaoService disponibilidadeTipoInstalacaoService;
    private final DemandaService demandaService;
    private final ProjetoPreValidadoService projetoPreValidadoService;

    public ChecklistController(
            ChecklistService checklistService,
            ValidacaoNormativaService validacaoNormativaService,
            DisponibilidadeTipoInstalacaoService disponibilidadeTipoInstalacaoService,
            DemandaService demandaService,
            ProjetoPreValidadoService projetoPreValidadoService) {
        this.checklistService = checklistService;
        this.validacaoNormativaService = validacaoNormativaService;
        this.disponibilidadeTipoInstalacaoService = disponibilidadeTipoInstalacaoService;
        this.demandaService = demandaService;
        this.projetoPreValidadoService = projetoPreValidadoService;
    }

    @PostMapping("/verificar")
    public ChecklistResponse verificar(@RequestBody(required = false) Edificacao edificacao) {
        return checklistService.verificar(edificacao);
    }

    @PostMapping("/enviar")
    public ResponseEntity<ChecklistResponse> enviar(
            @RequestBody(required = false) Edificacao edificacao) {

        ChecklistResponse checklist = checklistService.verificar(edificacao);
        ValidacaoResponse validacao =
                validacaoNormativaService.validarEdificacao(edificacao);
        String indisponibilidade =
                disponibilidadeTipoInstalacaoService.mensagemIndisponibilidade(edificacao);

        if (!checklist.liberadoParaEnvio() || !validacao.valido()
                || indisponibilidade != null) {
            List<String> pendencias = new ArrayList<>(checklist.pendencias());
            pendencias.addAll(validacao.alertas());
            if (indisponibilidade != null) {
                pendencias.add(indisponibilidade);
            }
            return ResponseEntity.badRequest().body(new ChecklistResponse(
                    false,
                    checklist.camposObrigatorios(),
                    List.copyOf(new LinkedHashSet<>(pendencias))
            ));
        }

        ResultadoDemanda resultado;
        try {
            resultado = demandaService.calcularDemanda(edificacao);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ChecklistResponse(
                    false,
                    checklist.camposObrigatorios(),
                    List.of(e.getMessage())
            ));
        }

        ProjetoPreValidado projeto = new ProjetoPreValidado(
                null,
                "Pré-validado",
                edificacao,
                resultado
        );

        projetoPreValidadoService.salvar(projeto);

        return ResponseEntity.ok(new ChecklistResponse(
                true,
                checklist.camposObrigatorios(),
                checklist.pendencias(),
                resultado
        ));
    }
}

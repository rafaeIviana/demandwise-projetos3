
package br.com.demandwise.controller;

import br.com.demandwise.dto.ChecklistResponse;
import br.com.demandwise.model.Apartamento;
import br.com.demandwise.model.Edificacao;
import br.com.demandwise.projeto.ProjetoPreValidado;
import br.com.demandwise.service.DemandaService;
import br.com.demandwise.service.ProjetoPreValidadoService;
import br.com.demandwise.service.ResultadoDemanda;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/checklist")
public class ChecklistController {

    private final DemandaService demandaService;
    private final ProjetoPreValidadoService projetoPreValidadoService;

    public ChecklistController(
            DemandaService demandaService,
            ProjetoPreValidadoService projetoPreValidadoService) {
        this.demandaService = demandaService;
        this.projetoPreValidadoService = projetoPreValidadoService;
    }

    @PostMapping("/verificar")
    public ChecklistResponse verificar(@RequestBody Edificacao edificacao) {
        return montarChecklist(edificacao);
    }

    // So libera o envio se o checklist estiver completo
    @PostMapping("/enviar")
    public ResponseEntity<ChecklistResponse> enviar(
            @RequestBody Edificacao edificacao) {

        ChecklistResponse checklist = montarChecklist(edificacao);

        if (!checklist.liberadoParaEnvio()) {
            return ResponseEntity.badRequest().body(checklist);
        }

        ResultadoDemanda resultado =
                demandaService.calcularDemanda(edificacao);

        ProjetoPreValidado projeto = new ProjetoPreValidado(
                null,
                "Pré-validado",
                edificacao,
                resultado
        );

        projetoPreValidadoService.salvar(projeto);

        return ResponseEntity.ok(checklist);
    }

    private ChecklistResponse montarChecklist(Edificacao edificacao) {
        List<String> camposObrigatorios = new ArrayList<>();
        List<String> pendencias = new ArrayList<>();

        camposObrigatorios.add("Apartamentos da edificação");
        List<Apartamento> apartamentos = edificacao.getApartamentos();

        if (apartamentos == null || apartamentos.isEmpty()) {
            pendencias.add("Nenhum apartamento foi informado");
        } else {
            for (int i = 0; i < apartamentos.size(); i++) {
                Apartamento apartamento = apartamentos.get(i);
                int numero = i + 1;

                camposObrigatorios.add(
                        "Apartamento " + numero + " - área útil");

                if (apartamento.getAreaUtil() <= 0) {
                    pendencias.add(
                            "Apartamento " + numero
                                    + ": área útil não informada");
                }

                camposObrigatorios.add(
                        "Apartamento " + numero + " - potência instalada");

                if (apartamento.getPotenciaInstalada() <= 0) {
                    pendencias.add(
                            "Apartamento " + numero
                                    + ": potência instalada não informada");
                }
            }
        }

        camposObrigatorios.add("Potência de iluminação");

        if (edificacao.getPotenciaIluminacao() <= 0) {
            pendencias.add("Potência de iluminação não informada");
        }

        camposObrigatorios.add("Potência de tomadas");

        if (edificacao.getPotenciaTomadas() <= 0) {
            pendencias.add("Potência de tomadas não informada");
        }

        boolean liberado = pendencias.isEmpty();

        return new ChecklistResponse(
                liberado,
                camposObrigatorios,
                pendencias
        );
    }
}

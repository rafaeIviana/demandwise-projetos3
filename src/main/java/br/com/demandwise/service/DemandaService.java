package br.com.demandwise.service;

import br.com.demandwise.model.Apartamento;
import br.com.demandwise.model.Edificacao;
import br.com.demandwise.model.GrupoResidencial;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Orchestrates the calculation while retaining the legacy input and output flow.
@Service
public class DemandaService {
    private final DemandaResidencialService demandaResidencialService;
    private final DemandaServicoService demandaServicoService;
    private final DemandaMotobombaService demandaMotobombaService;

    @Autowired
    public DemandaService(
            DemandaResidencialService demandaResidencialService,
            DemandaServicoService demandaServicoService,
            DemandaMotobombaService demandaMotobombaService) {
        this.demandaResidencialService = demandaResidencialService;
        this.demandaServicoService = demandaServicoService;
        this.demandaMotobombaService = demandaMotobombaService;
    }

    public DemandaService() {
        this(new DemandaResidencialService(), new DemandaServicoService(), new DemandaMotobombaService());
    }

    public ResultadoDemanda calcularDemanda(Edificacao edificacao) {
        List<CriterioAplicado> memoria = new ArrayList<>();
        List<Apartamento> apartamentos = edificacao.getApartamentos();
        List<GrupoResidencial> grupos = edificacao.getGruposResidenciais();

        demandaServicoService.validarRegrasDisponiveis(edificacao.getCargasCondominio());
        demandaMotobombaService.validarRegraDisponivel(edificacao.getMotobombaIncendio());

        if ((apartamentos == null || apartamentos.isEmpty()) && grupos.isEmpty()) {
            double demandaServicoUnica = calcularDemandaServico(edificacao, memoria);
            return new ResultadoDemanda(demandaServicoUnica, memoria);
        }

        DemandaResidencialService.CalculoResidencial residencial = demandaResidencialService.calcular(
                apartamentos, grupos, memoria);
        boolean possuiEntradasLegadasDeServico = edificacao.getPotenciaIluminacao() != 0
                || edificacao.getPotenciaTomadas() != 0
                || (edificacao.getPotenciasMotores() != null && !edificacao.getPotenciasMotores().isEmpty());
        double demandaServico = grupos.isEmpty() || possuiEntradasLegadasDeServico
                ? calcularDemandaServico(edificacao, memoria) : 0.0;
        double demandaTotal = residencial.demandaResidencialFinalDrf() + demandaServico;

        ResultadoDemandaDetalhado detalhamento = grupos.isEmpty()
                ? null : criarResultadoDetalhado(edificacao, residencial, demandaServico, demandaTotal, memoria);
        return new ResultadoDemanda(demandaTotal, memoria, detalhamento);
    }

    private ResultadoDemandaDetalhado criarResultadoDetalhado(
            Edificacao edificacao,
            DemandaResidencialService.CalculoResidencial residencial,
            double demandaServico,
            double demandaTotal,
            List<CriterioAplicado> memoria) {
        boolean possuiEntradasLegadasDeServico = edificacao.getPotenciaIluminacao() != 0
                || edificacao.getPotenciaTomadas() != 0
                || (edificacao.getPotenciasMotores() != null && !edificacao.getPotenciasMotores().isEmpty());

        ResultadoDemandaDetalhado.Condominio condominio = possuiEntradasLegadasDeServico
                ? new ResultadoDemandaDetalhado.Condominio(null, null, null, null, null, null,
                        BigDecimal.valueOf(demandaServico))
                : new ResultadoDemandaDetalhado.Condominio(BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO);

        List<ResultadoDemandaDetalhado.EtapaCalculo> etapas = memoria.stream()
                .map(this::criarEtapaDetalhada)
                .toList();

        return new ResultadoDemandaDetalhado(
                new ResultadoDemandaDetalhado.Residencial(
                        residencial.numeroUnidades(),
                        BigDecimal.valueOf(residencial.demandaResidencialBaseDr()),
                        BigDecimal.valueOf(residencial.fatorCoincidenciaFc()),
                        BigDecimal.valueOf(residencial.demandaCoincidenteDtr()),
                        BigDecimal.valueOf(residencial.fatorSegurancaFr()),
                        BigDecimal.valueOf(residencial.demandaResidencialFinalDrf())),
                condominio,
                new ResultadoDemandaDetalhado.Motobomba(BigDecimal.ZERO),
                BigDecimal.valueOf(demandaTotal),
                etapas
        );
    }

    private ResultadoDemandaDetalhado.EtapaCalculo criarEtapaDetalhada(CriterioAplicado criterio) {
        boolean fator = criterio.criterio().startsWith("FATOR_");
        String unidadeResultado = fator ? "adimensional"
                : criterio.criterio().equals("DEMANDA_MOTORES")
                ? "unidade original legada, sem conversão normativa"
                : "kVA";
        return new ResultadoDemandaDetalhado.EtapaCalculo(
                criterio.criterio(),
                criterio.descricao(),
                Map.of("parametro", criterio.parametro()),
                fator ? Map.of("valor", Double.toString(criterio.valorAplicado())) : Map.of(),
                BigDecimal.valueOf(criterio.valorAplicado()),
                unidadeResultado,
                criterio.criterio().equals("QUADRO_35") || fator
                        || criterio.criterio().equals("SUBTOTAL_GRUPO_RESIDENCIAL") ? "DIS-NOR-053" : null,
                criterio.criterio().equals("QUADRO_35") || fator
                        || criterio.criterio().equals("SUBTOTAL_GRUPO_RESIDENCIAL") ? "06" : null,
                criterio.criterio()
        );
    }

    private double calcularDemandaServico(Edificacao edificacao, List<CriterioAplicado> memoria) {
        double potenciaIluminacao = edificacao.getPotenciaIluminacao();
        double potenciaTomadas = edificacao.getPotenciaTomadas();
        List<Double> motores = edificacao.getPotenciasMotores();

        double demandaIluminacaoETomadas;
        if (motores != null && motores.size() == 4) {
            double potenciaTotal = potenciaIluminacao + potenciaTomadas;
            demandaIluminacaoETomadas = (potenciaTotal / 0.80) * 1.00;
            memoria.add(new CriterioAplicado(
                    "DEMANDA_ILUMINACAO_TOMADAS_4_MOTORES",
                    "potenciaIluminacao=" + potenciaIluminacao + ", potenciaTomadas=" + potenciaTomadas
                            + ", numeroMotores=4",
                    "Edificação com exatamente 4 motores -> iluminação e tomadas somadas com fator de "
                            + "demanda 1.00 sobre fator de potência 0.80",
                    demandaIluminacaoETomadas
            ));
        } else {
            double demandaIluminacao = (potenciaIluminacao / 0.80) * 1.00;
            double demandaTomadas = (potenciaTomadas / 0.80) * 0.50;
            demandaIluminacaoETomadas = demandaIluminacao + demandaTomadas;
            memoria.add(new CriterioAplicado(
                    "DEMANDA_ILUMINACAO_TOMADAS_PADRAO",
                    "potenciaIluminacao=" + potenciaIluminacao + ", potenciaTomadas=" + potenciaTomadas
                            + ", numeroMotores=" + (motores == null ? 0 : motores.size()),
                    "Regra padrão: iluminação com fator de demanda 1.00 e tomadas com fator de "
                            + "demanda 0.50, ambos sobre fator de potência 0.80",
                    demandaIluminacaoETomadas
            ));
        }

        return demandaIluminacaoETomadas + calcularDemandaMotores(motores, memoria);
    }

    private double calcularDemandaMotores(List<Double> potenciasMotores, List<CriterioAplicado> memoria) {
        if (potenciasMotores == null || potenciasMotores.isEmpty()) return 0.0;

        double maiorMotor = 0.0;
        double somaDemaisMotores = 0.0;
        for (Double potencia : potenciasMotores) {
            if (potencia == null || potencia <= 0) continue;
            if (potencia > maiorMotor) {
                if (maiorMotor > 0) somaDemaisMotores += maiorMotor;
                maiorMotor = potencia;
            } else {
                somaDemaisMotores += potencia;
            }
        }

        double demandaMotores = maiorMotor + (somaDemaisMotores * 0.50);
        memoria.add(new CriterioAplicado(
                "DEMANDA_MOTORES",
                "potenciasMotores=" + potenciasMotores,
                "Maior motor considerado a 100% (" + maiorMotor
                        + ") + demais motores a 50% (soma=" + somaDemaisMotores + ")",
                demandaMotores
        ));
        return demandaMotores;
    }
}

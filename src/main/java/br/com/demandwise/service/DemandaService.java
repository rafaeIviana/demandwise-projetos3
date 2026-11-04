package br.com.demandwise.service;

import br.com.demandwise.model.Apartamento;
import br.com.demandwise.model.Edificacao;
import br.com.demandwise.model.FaixaDemanda;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

import java.util.List;

//essa classe é a parte responsável por executar as regras do cálculo de demanda
@Service
public class DemandaService {

    private static final List<FaixaDemanda> QUADRO_35 = List.of(
            new FaixaDemanda(40, 1.00),
            new FaixaDemanda(45, 1.05),
            new FaixaDemanda(50, 1.16),
            new FaixaDemanda(55, 1.26),
            new FaixaDemanda(60, 1.36),
            new FaixaDemanda(65, 1.47),
            new FaixaDemanda(70, 1.57),
            new FaixaDemanda(75, 1.67),
            new FaixaDemanda(80, 1.76),
            new FaixaDemanda(85, 1.86),
            new FaixaDemanda(90, 1.96),
            new FaixaDemanda(95, 2.06),
            new FaixaDemanda(100, 2.16),
            new FaixaDemanda(110, 2.35),
            new FaixaDemanda(120, 2.54),
            new FaixaDemanda(130, 2.73),
            new FaixaDemanda(140, 2.91),
            new FaixaDemanda(150, 3.06),
            new FaixaDemanda(160, 3.28),
            new FaixaDemanda(170, 3.47),
            new FaixaDemanda(180, 3.65),
            new FaixaDemanda(190, 3.83),
            new FaixaDemanda(200, 4.01),
            new FaixaDemanda(220, 4.36),
            new FaixaDemanda(240, 4.72),
            new FaixaDemanda(260, 5.07),
            new FaixaDemanda(280, 5.42),
            new FaixaDemanda(300, 5.76),
            new FaixaDemanda(350, 6.61),
            new FaixaDemanda(400, 7.45),
            new FaixaDemanda(450, 8.28),
            new FaixaDemanda(500, 9.10),
            new FaixaDemanda(550, 9.91),
            new FaixaDemanda(600, 10.71),
            new FaixaDemanda(650, 11.51),
            new FaixaDemanda(700, 12.30),
            new FaixaDemanda(800, 13.86),
            new FaixaDemanda(900, 15.40),
            new FaixaDemanda(1000, 16.93)
    );

public ResultadoDemanda calcularDemanda(Edificacao edificacao) {
    List<CriterioAplicado> memoria = new ArrayList<>();
        List<Apartamento> apartamentos = edificacao.getApartamentos();

        if (apartamentos == null || apartamentos.isEmpty()) {
    double demandaServicoUnica = calcularDemandaServico(edificacao, memoria);
    return new ResultadoDemanda(demandaServicoUnica, memoria);
        }

        double demandaTotal = 0.0;

       for (int i = 0; i < apartamentos.size(); i++) {
    Apartamento apartamento = apartamentos.get(i);
    demandaTotal += calcularDemandaPorArea(apartamento.getAreaUtil(), i, memoria);
}
    

        double fatorCoincidencia =
               obterFatorCoincidencia(apartamentos.size(), memoria);

        double demandaComCoincidencia =
                demandaTotal * fatorCoincidencia;

        double fatorSeguranca =
               obterFatorSeguranca(demandaComCoincidencia, memoria);

        double demandaResidencialFinal =
                demandaComCoincidencia * fatorSeguranca;

        double demandaServico =
               calcularDemandaServico(edificacao, memoria);

       double demandaTotalFinal = demandaResidencialFinal + demandaServico;
       return new ResultadoDemanda(demandaTotalFinal, memoria);
    }

private double calcularDemandaPorArea(
        double areaUtil, int indiceApartamento, List<CriterioAplicado> memoria) {

    if (areaUtil <= 0) {
        throw new IllegalArgumentException(
                "A área útil deve ser maior que zero."
        );
    }

    for (FaixaDemanda faixa : QUADRO_35) {

        if (areaUtil <= faixa.getAreaMaxima()) {
            memoria.add(new CriterioAplicado(
                    "QUADRO_35",
                    "apartamento[" + indiceApartamento + "].areaUtil=" + areaUtil,
                    "Área útil até " + faixa.getAreaMaxima() + " m² -> demanda unitária "
                            + faixa.getDemanda() + " kVA",
                    faixa.getDemanda()
            ));
            return faixa.getDemanda();
        }
    }

    throw new IllegalArgumentException(
            "Área útil acima de 1000 m² não possui valor definido no Quadro 35."
    );
}

    private double obterFatorCoincidencia(int numeroApartamentos, List<CriterioAplicado> memoria) {

        double[] fatores = {
                1.0000, 0.9800, 0.9730, 0.9700, 0.9680,
                0.9660, 0.9657, 0.9650, 0.9645, 0.9640,
                0.9473, 0.9333, 0.9215, 0.9114, 0.9027,
                0.8950, 0.8882, 0.8822, 0.8768, 0.8720,
                0.8590, 0.8477, 0.8370, 0.8275, 0.8184,
                0.8100, 0.8026, 0.7954, 0.7890, 0.7827,
                0.7768, 0.7716, 0.7664, 0.7618, 0.7571,
                0.7528, 0.7489, 0.7445, 0.7411, 0.7380,
                0.7346, 0.7317, 0.7289, 0.7260, 0.7231,
                0.7196, 0.7162, 0.7129, 0.7098, 0.7068,
                0.7039, 0.7017, 0.6985, 0.6960, 0.6935,
                0.6911, 0.6888, 0.6866, 0.6844
        };

        double fatorEscolhido;
        String descricao;

        if (numeroApartamentos <= 0) {
            fatorEscolhido = 0.0;
            descricao = "Número de apartamentos <= 0 -> fator de coincidência 0.0";
        } else if (numeroApartamentos <= fatores.length) {
            fatorEscolhido = fatores[numeroApartamentos - 1];
            descricao = "Fator de coincidência tabelado para " + numeroApartamentos + " apartamento(s)";
        } else {
            fatorEscolhido = 0.6823;
            descricao = "Acima de " + fatores.length + " apartamentos -> fator mínimo tabelado 0.6823";
        }

        memoria.add(new CriterioAplicado(
                "FATOR_COINCIDENCIA",
                "numeroApartamentos=" + numeroApartamentos,
                descricao,
                fatorEscolhido
        ));

        return fatorEscolhido;
    }

private double obterFatorSeguranca(double demanda, List<CriterioAplicado> memoria) {

        double fator;
        String descricao;

        if (demanda <= 25) {
            fator = 1.5;
            descricao = "Demanda com coincidência <= 25 kVA -> fator de segurança 1.5";
        } else if (demanda <= 50) {
            fator = 1.3;
            descricao = "Demanda com coincidência <= 50 kVA -> fator de segurança 1.3";
        } else if (demanda <= 100) {
            fator = 1.2;
            descricao = "Demanda com coincidência <= 100 kVA -> fator de segurança 1.2";
        } else {
            fator = 1.1;
            descricao = "Demanda com coincidência > 100 kVA -> fator de segurança 1.1";
        }

        memoria.add(new CriterioAplicado(
                "FATOR_SEGURANCA",
                "demandaComCoincidencia=" + demanda,
                descricao,
                fator
        ));

        return fator;
    }

    private double calcularDemandaServico(Edificacao edificacao, List<CriterioAplicado> memoria) {

        double potenciaIluminacao =
                edificacao.getPotenciaIluminacao();

        double potenciaTomadas =
                edificacao.getPotenciaTomadas();

        List<Double> motores =
                edificacao.getPotenciasMotores();

        double demandaIluminacaoETomadas;

        if (motores != null && motores.size() == 4) {

            double potenciaTotal =
                    potenciaIluminacao + potenciaTomadas;

            demandaIluminacaoETomadas =
                    (potenciaTotal / 0.80) * 1.00;

            memoria.add(new CriterioAplicado(
                    "DEMANDA_ILUMINACAO_TOMADAS_4_MOTORES",
                    "potenciaIluminacao=" + potenciaIluminacao + ", potenciaTomadas=" + potenciaTomadas
                            + ", numeroMotores=4",
                    "Edificação com exatamente 4 motores -> iluminação e tomadas somadas com fator de "
                            + "demanda 1.00 sobre fator de potência 0.80",
                    demandaIluminacaoETomadas
            ));

        } else {

            double demandaIluminacao =
                    (potenciaIluminacao / 0.80) * 1.00;

            double demandaTomadas =
                    (potenciaTomadas / 0.80) * 0.50;

            demandaIluminacaoETomadas =
                    demandaIluminacao + demandaTomadas;

            memoria.add(new CriterioAplicado(
                    "DEMANDA_ILUMINACAO_TOMADAS_PADRAO",
                    "potenciaIluminacao=" + potenciaIluminacao + ", potenciaTomadas=" + potenciaTomadas
                            + ", numeroMotores=" + (motores == null ? 0 : motores.size()),
                    "Regra padrão: iluminação com fator de demanda 1.00 e tomadas com fator de "
                            + "demanda 0.50, ambos sobre fator de potência 0.80",
                    demandaIluminacaoETomadas
            ));
        }

        double demandaMotores =
                calcularDemandaMotores(motores, memoria);

        return demandaIluminacaoETomadas + demandaMotores;
    }
private double calcularDemandaMotores(
        List<Double> potenciasMotores, List<CriterioAplicado> memoria) {

        if (potenciasMotores == null ||
                potenciasMotores.isEmpty()) {

            return 0.0;
        }

        double maiorMotor = 0.0;
        double somaDemaisMotores = 0.0;

        for (Double potencia : potenciasMotores) {

            if (potencia == null || potencia <= 0) {
                continue;
            }

            if (potencia > maiorMotor) {

                if (maiorMotor > 0) {
                    somaDemaisMotores += maiorMotor;
                }

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
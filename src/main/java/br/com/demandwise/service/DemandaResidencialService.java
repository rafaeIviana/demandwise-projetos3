package br.com.demandwise.service;

import br.com.demandwise.model.Apartamento;
import br.com.demandwise.model.FaixaDemanda;
import br.com.demandwise.model.GrupoResidencial;
import org.springframework.stereotype.Service;

import java.util.List;

/** Residential calculation using the same tables and thresholds as the legacy engine. */
@Service
public class DemandaResidencialService {
    private static final double FATOR_COINCIDENCIA_SESSENTA_OU_MAIS = 0.6823;

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

    public CalculoResidencial calcular(
            List<Apartamento> apartamentos,
            List<GrupoResidencial> gruposResidenciais,
            List<CriterioAplicado> memoria) {
        List<Apartamento> unidades = apartamentos == null ? List.of() : apartamentos;
        List<GrupoResidencial> grupos = gruposResidenciais == null ? List.of() : gruposResidenciais;
        if (!unidades.isEmpty() && !grupos.isEmpty()) {
            throw new IllegalArgumentException(
                    "Informe apartamentos individuais ou grupos residenciais, sem misturar as duas representações.");
        }

        double demandaResidencialBase = 0.0;
        int quantidadeUnidades = 0;
        for (int i = 0; i < unidades.size(); i++) {
            Apartamento apartamento = unidades.get(i);
            demandaResidencialBase += calcularDemandaPorArea(
                    apartamento.getAreaUtil(), "apartamento[" + i + "]", memoria);
            quantidadeUnidades++;
        }

        for (GrupoResidencial grupo : grupos) {
            double areaMinima = grupo.areaMinimaM2().doubleValue();
            double areaMaxima = grupo.areaMaximaM2().doubleValue();
            double demandaMinima = demandaPorArea(areaMinima);
            double demandaMaxima = demandaPorArea(areaMaxima);
            if (Double.compare(demandaMinima, demandaMaxima) != 0) {
                throw new IllegalArgumentException("O grupo " + grupo.tipologia()
                        + " atravessa mais de uma faixa do Quadro 35 existente; divida o grupo por faixa.");
            }

            double subtotalGrupo = demandaMinima * grupo.quantidade();
            demandaResidencialBase += subtotalGrupo;
            quantidadeUnidades += grupo.quantidade();
            memoria.add(new CriterioAplicado(
                    "QUADRO_35",
                    "grupo=" + grupo.tipologia() + ", area=" + areaMinima + "-" + areaMaxima
                            + " m², quantidade=" + grupo.quantidade(),
                    "Demanda unitária existente de " + demandaMinima + " kVA x "
                            + grupo.quantidade() + " unidade(s)",
                    demandaMinima
            ));
            memoria.add(new CriterioAplicado(
                    "SUBTOTAL_GRUPO_RESIDENCIAL",
                    "grupo=" + grupo.tipologia() + ", quantidade=" + grupo.quantidade(),
                    demandaMinima + " kVA x " + grupo.quantidade(),
                    subtotalGrupo
            ));
        }

        if (quantidadeUnidades == 0) {
            return new CalculoResidencial(0, 0, 0, 0, 0, 0);
        }

        double fatorCoincidencia = obterFatorCoincidencia(quantidadeUnidades, memoria);
        double demandaCoincidente = demandaResidencialBase * fatorCoincidencia;
        // DIS-NOR-053 Rev.06 defines Drf = Dtr x Fr. Keep full precision here;
        // the available normative text does not define intermediate rounding.
        double fatorSeguranca = obterFatorSeguranca(demandaResidencialBase, memoria);
        double demandaResidencialFinal = demandaCoincidente * fatorSeguranca;

        return new CalculoResidencial(demandaResidencialBase, quantidadeUnidades,
                fatorCoincidencia, demandaCoincidente, fatorSeguranca, demandaResidencialFinal);
    }

    private double calcularDemandaPorArea(double area, String origem, List<CriterioAplicado> memoria) {
        double demanda = demandaPorArea(area);
        double areaMaximaDaFaixa = QUADRO_35.stream()
                .filter(faixa -> area <= faixa.getAreaMaxima())
                .findFirst()
                .orElseThrow()
                .getAreaMaxima();
        memoria.add(new CriterioAplicado(
                "QUADRO_35",
                origem + ".areaUtil=" + area,
                "Área útil até " + areaMaximaDaFaixa + " m² -> demanda unitária " + demanda + " kVA",
                demanda
        ));
        return demanda;
    }

    private double demandaPorArea(double areaUtil) {
        if (areaUtil <= 0) {
            throw new IllegalArgumentException("A área útil deve ser maior que zero.");
        }
        return QUADRO_35.stream()
                .filter(faixa -> areaUtil <= faixa.getAreaMaxima())
                .findFirst()
                .map(FaixaDemanda::getDemanda)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Área útil acima de 1000 m² não possui valor definido no Quadro 35."));
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
            // DIS-NOR-053 Rev.06 defines an explicit "60 or more" band.
            // Special smart/studio typologies need a separately confirmed rule.
            fatorEscolhido = FATOR_COINCIDENCIA_SESSENTA_OU_MAIS;
            descricao = "60 ou mais apartamentos -> fator de coincidência tabelado 0.6823";
        }

        memoria.add(new CriterioAplicado("FATOR_COINCIDENCIA",
                "numeroApartamentos=" + numeroApartamentos, descricao, fatorEscolhido));
        return fatorEscolhido;
    }

    private double obterFatorSeguranca(double demandaResidencialDr, List<CriterioAplicado> memoria) {
        double fator;
        String descricao;
        if (demandaResidencialDr <= 25) {
            fator = 1.5;
            descricao = "Dr <= 25 kVA -> fator de segurança 1.5";
        } else if (demandaResidencialDr <= 50) {
            fator = 1.3;
            descricao = "25 kVA < Dr <= 50 kVA -> fator de segurança 1.3";
        } else if (demandaResidencialDr <= 100) {
            fator = 1.2;
            descricao = "50 kVA < Dr <= 100 kVA -> fator de segurança 1.2";
        } else {
            fator = 1.1;
            descricao = "Dr > 100 kVA -> fator de segurança 1.1";
        }
        memoria.add(new CriterioAplicado("FATOR_SEGURANCA",
                "demandaResidencialDr=" + demandaResidencialDr, descricao, fator));
        return fator;
    }

    public record CalculoResidencial(
            double demandaResidencialBaseDr,
            int numeroUnidades,
            double fatorCoincidenciaFc,
            double demandaCoincidenteDtr,
            double fatorSegurancaFr,
            double demandaResidencialFinalDrf
    ) { }
}

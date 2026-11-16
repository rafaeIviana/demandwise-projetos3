package br.com.demandwise.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Structured residential details are populated when the request uses residential groups. */
public record ResultadoDemandaDetalhado(
        Residencial residencial,
        Condominio condominio,
        Motobomba motobomba,
        BigDecimal demandaTotalEdificacaoKva,
        List<EtapaCalculo> memoriaCalculo
) {
    public ResultadoDemandaDetalhado {
        memoriaCalculo = memoriaCalculo == null ? List.of() : List.copyOf(memoriaCalculo);
    }

    public record Residencial(
            int numeroUnidades,
            BigDecimal demandaResidencialBaseDrKva,
            BigDecimal fatorCoincidenciaFc,
            BigDecimal demandaCoincidenteDtrKva,
            BigDecimal fatorSegurancaFr,
            BigDecimal demandaResidencialFinalDrfKva
    ) { }

    public record Condominio(
            BigDecimal iluminacaoKva,
            BigDecimal tomadasUsoGeralKva,
            BigDecimal arCondicionadoKva,
            BigDecimal motoresKva,
            BigDecimal equipamentosEspeciaisKva,
            BigDecimal bombasHidromassagemKva,
            BigDecimal demandaServicoDsKva
    ) { }

    public record Motobomba(BigDecimal demandaDmbiKva) { }

    public record EtapaCalculo(
            String etapa,
            String formula,
            Map<String, String> entradas,
            Map<String, String> fatores,
            BigDecimal resultado,
            String unidadeResultado,
            String norma,
            String revisao,
            String tabelaOuItem
    ) {
        public EtapaCalculo {
            entradas = entradas == null ? Map.of() : Map.copyOf(entradas);
            fatores = fatores == null ? Map.of() : Map.copyOf(fatores);
        }
    }
}

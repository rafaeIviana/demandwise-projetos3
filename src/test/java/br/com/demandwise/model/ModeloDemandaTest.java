package br.com.demandwise.model;

import br.com.demandwise.service.CriterioAplicado;
import br.com.demandwise.service.ResultadoDemanda;
import br.com.demandwise.service.ResultadoDemandaDetalhado;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ModeloDemandaTest {

    @Test
    void potenciaMantemValorDecimalEUnidadeOriginais() {
        Potencia motor = new Potencia(new BigDecimal("12.5"), UnidadePotencia.CV);
        Potencia arCondicionado = new Potencia(new BigDecimal("14.60"), UnidadePotencia.KW);

        assertEquals(new BigDecimal("12.5"), motor.valor());
        assertEquals(UnidadePotencia.CV, motor.unidade());
        assertEquals(new BigDecimal("14.60"), arCondicionado.valor());
        assertEquals(UnidadePotencia.KW, arCondicionado.unidade());
    }

    @Test
    void potenciaEQuantidadeInvalidasSaoRejeitadas() {
        assertThrows(IllegalArgumentException.class,
                () -> new Potencia(BigDecimal.ZERO, UnidadePotencia.KW));
        assertThrows(IllegalArgumentException.class,
                () -> new Potencia(BigDecimal.ONE, null));
        assertThrows(IllegalArgumentException.class,
                () -> new CargaInstalada("Bomba", new Potencia(BigDecimal.ONE, UnidadePotencia.CV), 0));
    }

    @Test
    void cargasCondominioPreservaCategoriasQuantidadeIdentificacaoEUnidade() {
        CargaInstalada iluminacao = new CargaInstalada(
                "Bloco A", new Potencia(new BigDecimal("5.10"), UnidadePotencia.KW), 1);
        CargaInstalada tomada = new CargaInstalada(
                null, new Potencia(new BigDecimal("6.70"), UnidadePotencia.KW), 1);
        CargaInstalada ar = new CargaInstalada(
                "Splits", new Potencia(new BigDecimal("18000"), UnidadePotencia.BTU_POR_HORA), 8);
        CargaInstalada motor = new CargaInstalada(
                "Bomba principal", new Potencia(new BigDecimal("12.5"), UnidadePotencia.CV), 1);
        CargaInstalada especial = new CargaInstalada(
                "Tomada específica", new Potencia(new BigDecimal("6.70"), UnidadePotencia.KW), 1);
        CargaInstalada bomba = new CargaInstalada(
                "Hidromassagem", new Potencia(new BigDecimal("2"), UnidadePotencia.CV), 2);

        CargasCondominio cargas = new CargasCondominio(
                List.of(iluminacao), List.of(tomada), List.of(ar), List.of(motor),
                List.of(especial), List.of(bomba));

        assertEquals(iluminacao, cargas.iluminacao().getFirst());
        assertEquals(tomada, cargas.tomadasUsoGeral().getFirst());
        assertEquals(8, cargas.arCondicionado().getFirst().quantidade());
        assertEquals(UnidadePotencia.CV, cargas.motores().getFirst().potencia().unidade());
        assertEquals(especial, cargas.equipamentosEspeciais().getFirst());
        assertEquals(2, cargas.bombasHidromassagem().getFirst().quantidade());
        assertThrows(UnsupportedOperationException.class,
                () -> cargas.motores().add(motor));
    }

    @Test
    void grupoResidencialRepresentaFaixaQuantidadeETipologiaSemDiManual() {
        GrupoResidencial grupo = new GrupoResidencial(
                "A", "Faixa A", new BigDecimal("61"), new BigDecimal("65"), 50);

        assertEquals("Faixa A", grupo.tipologia());
        assertEquals(new BigDecimal("61"), grupo.areaMinimaM2());
        assertEquals(new BigDecimal("65"), grupo.areaMaximaM2());
        assertEquals(50, grupo.quantidade());
        assertThrows(IllegalArgumentException.class, () -> new GrupoResidencial(
                "B", "Faixa B", new BigDecimal("70"), new BigDecimal("66"), 50));
    }

    @Test
    void motobombaExigePotenciaNominalERepresentaAusencia() {
        MotobombaIncendio bomba = new MotobombaIncendio(
                true, "MBI", new Potencia(new BigDecimal("2"), UnidadePotencia.CV));

        assertTrue(bomba.presente());
        assertEquals(UnidadePotencia.CV, bomba.potenciaNominal().unidade());
        assertFalse(MotobombaIncendio.ausente().presente());
        assertThrows(IllegalArgumentException.class,
                () -> new MotobombaIncendio(true, "MBI", null));
        assertThrows(IllegalArgumentException.class,
                () -> new MotobombaIncendio(false, null,
                        new Potencia(BigDecimal.ONE, UnidadePotencia.KW)));
    }

    @Test
    void edificacaoMantemApartamentosECamposLegadosEArmazenaNovosBlocos() {
        Apartamento apartamento = new Apartamento(64, 16.33, 0, false);
        Edificacao edificacao = new Edificacao(List.of(apartamento));
        GrupoResidencial grupo = new GrupoResidencial(
                "A", "Faixa A", new BigDecimal("61"), new BigDecimal("65"), 50);
        edificacao.setGruposResidenciais(List.of(grupo));
        edificacao.setPotenciaIluminacao(5.1);
        edificacao.setPotenciaTomadas(6.7);
        edificacao.setPotenciasMotores(List.of(12.5));

        assertEquals(1, edificacao.getApartamentos().size());
        assertEquals(64, edificacao.getApartamentos().getFirst().getAreaUtil());
        assertEquals(1, edificacao.getGruposResidenciais().size());
        assertEquals(5.1, edificacao.getPotenciaIluminacao());
        assertEquals(6.7, edificacao.getPotenciaTomadas());
        assertEquals(List.of(12.5), edificacao.getPotenciasMotores());
        assertNotNull(edificacao.getCargasCondominio());
        assertFalse(edificacao.getMotobombaIncendio().presente());
    }

    @Test
    void resultadoDetalhadoPodeRepresentarBlocosSemAlterarContratoAtual() {
        ResultadoDemanda atual = new ResultadoDemanda(12.345, List.of(
                new CriterioAplicado("REGRA_ATUAL", "entrada", "descrição", 12.345)));
        ResultadoDemandaDetalhado.EtapaCalculo etapa = new ResultadoDemandaDetalhado.EtapaCalculo(
                "Dtr", "Dr x Fc", Map.of("Dr", "152 kVA"), Map.of("Fc", "0.6823"),
                new BigDecimal("103.7144"), "kVA", "DIS-NOR-053", "06", "Quadro 36");
        ResultadoDemandaDetalhado futuro = new ResultadoDemandaDetalhado(
                new ResultadoDemandaDetalhado.Residencial(100, new BigDecimal("152"),
                        new BigDecimal("0.6823"), new BigDecimal("103.7144"),
                        new BigDecimal("1.1"), new BigDecimal("114")),
                new ResultadoDemandaDetalhado.Condominio(null, null, null, null, null, null,
                        new BigDecimal("69.40")),
                new ResultadoDemandaDetalhado.Motobomba(new BigDecimal("2.70")),
                new BigDecimal("186.10"), List.of(etapa));

        assertEquals(12.35, atual.demandaTotalArredondada(), 0.0001);
        assertEquals(100, futuro.residencial().numeroUnidades());
        assertEquals(new BigDecimal("69.40"), futuro.condominio().demandaServicoDsKva());
        assertEquals(new BigDecimal("2.70"), futuro.motobomba().demandaDmbiKva());
        assertEquals("Quadro 36", futuro.memoriaCalculo().getFirst().tabelaOuItem());
    }
}

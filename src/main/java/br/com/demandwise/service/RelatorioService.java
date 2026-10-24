package br.com.demandwise.service;

import br.com.demandwise.model.Apartamento;
import br.com.demandwise.model.Edificacao;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.awt.Color;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RelatorioService {

    public byte[] gerarPdf(Edificacao edificacao, ResultadoDemanda resultado) {
        ByteArrayOutputStream arquivo = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4.rotate(), 40, 40, 36, 36);
        PdfWriter.getInstance(documento, arquivo);
        documento.open();

        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font negrito = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font cabecalhoTabela = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font textoTabela = FontFactory.getFont(FontFactory.HELVETICA, 9);

        String data = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        documento.add(new Paragraph("MEMORIAL DE CÁLCULO DE DEMANDA", titulo));
        documento.add(new Paragraph("Edificação de uso coletivo - Norma DIS-NOR-053", normal));
        documento.add(new Paragraph("Tipo de instalação: " + edificacao.getTipoInstalacao().getDescricao(), normal));
        documento.add(new Paragraph("Data de emissão: " + data, normal));
        documento.add(new Paragraph(" "));

        documento.add(new Paragraph("1. Dados de entrada", negrito));
        documento.add(new Paragraph("Número de apartamentos: " + edificacao.getNumeroApartamentos(), normal));

        List<Apartamento> apartamentos = edificacao.getApartamentos();
        for (int i = 0; i < apartamentos.size(); i++) {
            Apartamento apartamento = apartamentos.get(i);
            int numero = i + 1;
            String estudoDeRede = apartamento.isPossuiEstudoDeRede() ? "sim" : "não";

            documento.add(new Paragraph("Apartamento " + numero
                    + ": área útil " + formatarPrecisao(apartamento.getAreaUtil()) + " m²"
                    + ", potência instalada " + formatarPrecisao(apartamento.getPotenciaInstalada()) + " kW"
                    + ", recarga de veículo elétrico " + formatarPrecisao(apartamento.getPotenciaRecargaVe()) + " kW"
                    + ", estudo de rede: " + estudoDeRede, normal));
        }

        documento.add(new Paragraph("Potência de iluminação: "
                + formatarPrecisao(edificacao.getPotenciaIluminacao()) + " kW", normal));
        documento.add(new Paragraph("Potência de tomadas: "
                + formatarPrecisao(edificacao.getPotenciaTomadas()) + " kW", normal));
        documento.add(new Paragraph("Motores: " + formatarMotores(edificacao.getPotenciasMotores()), normal));
        documento.add(new Paragraph(" "));

        documento.add(new Paragraph("2. Memória de cálculo", negrito));
        documento.add(new Paragraph(" "));

        PdfPTable tabela = new PdfPTable(4);
        tabela.setWidthPercentage(100);
        tabela.setWidths(new float[]{2.1f, 3.5f, 4.8f, 1.35f});
        tabela.setHeaderRows(1);
        tabela.setSplitRows(false);
        adicionarCelulaCabecalho(tabela, "Critério", cabecalhoTabela);
        adicionarCelulaCabecalho(tabela, "Parâmetro", cabecalhoTabela);
        adicionarCelulaCabecalho(tabela, "Descrição", cabecalhoTabela);
        adicionarCelulaCabecalho(tabela, "Valor aplicado", cabecalhoTabela);

        for (CriterioAplicado criterio : resultado.memoriaCalculo()) {
            tabela.addCell(criarCelula(criterio.criterio(), textoTabela, false));
            tabela.addCell(criarCelula(criterio.parametro(), textoTabela, false));
            tabela.addCell(criarCelula(criterio.descricao(), textoTabela, false));
            tabela.addCell(criarCelula(formatarPrecisao(criterio.valorAplicado()), textoTabela, false));
        }
        documento.add(tabela);
        documento.add(new Paragraph(" "));

        documento.add(new Paragraph("3. Resultado", negrito));
        documento.add(new Paragraph("Demanda total da edificação: "
                + formatarResultado(resultado.demandaTotalArredondada()) + " kVA", negrito));

        documento.close();
        return arquivo.toByteArray();
    }

    private void adicionarCelulaCabecalho(PdfPTable tabela, String texto, Font fonte) {
        tabela.addCell(criarCelula(texto, fonte, true));
    }

    private PdfPCell criarCelula(String texto, Font fonte, boolean cabecalho) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, fonte));
        celula.setPaddingTop(5);
        celula.setPaddingBottom(5);
        celula.setPaddingLeft(6);
        celula.setPaddingRight(6);
        celula.setVerticalAlignment(PdfPCell.ALIGN_TOP);
        celula.setBorderColor(new Color(203, 210, 217));
        if (cabecalho) {
            celula.setBackgroundColor(new Color(239, 246, 255));
        }
        return celula;
    }

    private String formatarMotores(List<Double> motores) {
        if (motores == null || motores.isEmpty()) {
            return "nenhum";
        }

        StringBuilder valores = new StringBuilder();
        for (Double potencia : motores) {
            if (potencia != null) {
                if (!valores.isEmpty()) {
                    valores.append("; ");
                }
                valores.append(formatarPrecisao(potencia)).append(" kW");
            }
        }
        return valores.isEmpty() ? "nenhum" : valores.toString();
    }

    private String formatarResultado(double valor) {
        return String.format(java.util.Locale.forLanguageTag("pt-BR"), "%.2f", valor);
    }

    private String formatarPrecisao(double valor) {
        return BigDecimal.valueOf(valor).stripTrailingZeros().toPlainString().replace('.', ',');
    }
}

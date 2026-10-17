package br.com.demandwise.controller;

import br.com.demandwise.model.Apartamento;
import br.com.demandwise.model.Edificacao;
import br.com.demandwise.model.TipoInstalacao;
import br.com.demandwise.service.CriterioAplicado;
import br.com.demandwise.service.DemandaService;
import br.com.demandwise.service.ResultadoDemanda;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/relatorio")
public class RelatorioController {

    private final DemandaService demandaService;

    public RelatorioController(DemandaService demandaService) {
        this.demandaService = demandaService;
    }

    @PostMapping("/pdf")
    public ResponseEntity<?> gerarPdf(@RequestBody Edificacao edificacao) {
        TipoInstalacao tipo = edificacao.getTipoInstalacao();

        if (!tipo.isDisponivel()) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of(
                    "mensagem", "O relatório para o tipo " + tipo.getDescricao() + " estará disponível em breve."));
        }

        ResultadoDemanda resultado;
        try {
            resultado = demandaService.calcularDemanda(edificacao);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", e.getMessage()));
        }

        byte[] pdf = montarPdf(edificacao, resultado);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=\"relatorio-calculo-demanda.pdf\"")
                .body(pdf);
    }

    private byte[] montarPdf(Edificacao edificacao, ResultadoDemanda resultado) {
        ByteArrayOutputStream arquivo = new ByteArrayOutputStream();
        Document documento = new Document();
        PdfWriter.getInstance(documento, arquivo);
        documento.open();

        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font negrito = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);

        String data = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        documento.add(new Paragraph("MEMORIAL DE CÁLCULO DE DEMANDA", titulo));
        documento.add(new Paragraph("Edificação de uso coletivo - Norma DIS-NOR-053", normal));
        documento.add(new Paragraph("Tipo de instalação: " + edificacao.getTipoInstalacao().getDescricao(), normal));
        documento.add(new Paragraph("Data de emissão: " + data, normal));
        documento.add(new Paragraph(" "));

        documento.add(new Paragraph("1. Dados de entrada", negrito));
        documento.add(new Paragraph("Número de apartamentos: " + edificacao.getNumeroApartamentos(), normal));

        List<Apartamento> apartamentos = edificacao.getApartamentos();
        if (apartamentos != null) {
            for (int i = 0; i < apartamentos.size(); i++) {
                Apartamento apartamento = apartamentos.get(i);
                int numero = i + 1;

                String estudoDeRede = "não";
                if (apartamento.isPossuiEstudoDeRede()) {
                    estudoDeRede = "sim";
                }

                documento.add(new Paragraph("Apartamento " + numero
                        + ": área útil " + formatar(apartamento.getAreaUtil()) + " m²"
                        + ", potência instalada " + formatar(apartamento.getPotenciaInstalada()) + " kW"
                        + ", recarga de veículo elétrico " + formatar(apartamento.getPotenciaRecargaVe()) + " kW"
                        + ", estudo de rede: " + estudoDeRede, normal));
            }
        }

        documento.add(new Paragraph("Potência de iluminação: " + formatar(edificacao.getPotenciaIluminacao()) + " kW", normal));
        documento.add(new Paragraph("Potência de tomadas: " + formatar(edificacao.getPotenciaTomadas()) + " kW", normal));

        String motores = "nenhum";
        if (edificacao.getPotenciasMotores() != null && !edificacao.getPotenciasMotores().isEmpty()) {
            motores = "";
            for (Double potencia : edificacao.getPotenciasMotores()) {
                if (potencia != null) {
                    motores += formatar(potencia) + " kW; ";
                }
            }
        }
        documento.add(new Paragraph("Motores: " + motores, normal));
        documento.add(new Paragraph(" "));

        documento.add(new Paragraph("2. Memória de cálculo", negrito));
        documento.add(new Paragraph(" "));

        PdfPTable tabela = new PdfPTable(3);
        tabela.setWidthPercentage(100);
        tabela.addCell(new Phrase("Critério", negrito));
        tabela.addCell(new Phrase("Descrição", negrito));
        tabela.addCell(new Phrase("Valor aplicado", negrito));

        for (CriterioAplicado criterio : resultado.memoriaCalculo()) {
            tabela.addCell(new Phrase(criterio.criterio(), normal));
            tabela.addCell(new Phrase(criterio.descricao(), normal));
            tabela.addCell(new Phrase(String.format(new Locale("pt", "BR"), "%.3f", criterio.valorAplicado()), normal));
        }
        documento.add(tabela);
        documento.add(new Paragraph(" "));

        documento.add(new Paragraph("3. Resultado", negrito));
        documento.add(new Paragraph("Demanda total da edificação: " + formatar(resultado.demandaTotal()) + " kVA", negrito));

        documento.close();
        return arquivo.toByteArray();
    }

    private String formatar(double valor) {
        return String.format(new Locale("pt", "BR"), "%.2f", valor);
    }
}

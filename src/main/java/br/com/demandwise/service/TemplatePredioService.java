package br.com.demandwise.service;

import br.com.demandwise.model.Apartamento;
import br.com.demandwise.model.Edificacao;
import br.com.demandwise.projeto.TemplatePredio;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TemplatePredioService {

    private final List<TemplatePredio> templates = new CopyOnWriteArrayList<>();
    private final AtomicLong proximoId = new AtomicLong(1);

    public TemplatePredio salvar(String nome, Edificacao edificacao) {
        TemplatePredio template = new TemplatePredio(
                proximoId.getAndIncrement(),
                nome.trim(),
                copiar(edificacao)
        );
        templates.add(template);
        return comCopia(template);
    }

    public List<TemplatePredio> listarTodos() {
        return templates.stream().map(this::comCopia).toList();
    }

    public Optional<TemplatePredio> buscarPorId(Long id) {
        return templates.stream()
                .filter(t -> t.id().equals(id))
                .findFirst()
                .map(this::comCopia);
    }

    // Sempre devolvemos uma cópia: editar o formulário nunca altera o template salvo.
    private TemplatePredio comCopia(TemplatePredio t) {
        return new TemplatePredio(t.id(), t.nome(), copiar(t.edificacao()));
    }

    static Edificacao copiar(Edificacao origem) {
        Edificacao copia = new Edificacao();
        if (origem.getApartamentos() != null) {
            List<Apartamento> aptos = new ArrayList<>();
            for (Apartamento a : origem.getApartamentos()) {
                aptos.add(new Apartamento(
                        a.getAreaUtil(),
                        a.getPotenciaInstalada(),
                        a.getPotenciaRecargaVe(),
                        a.isPossuiEstudoDeRede()
                ));
            }
            copia.setApartamentos(aptos);
        }
        copia.setPotenciaIluminacao(origem.getPotenciaIluminacao());
        copia.setPotenciaTomadas(origem.getPotenciaTomadas());
        if (origem.getPotenciasMotores() != null) {
            copia.setPotenciasMotores(new ArrayList<>(origem.getPotenciasMotores()));
        }
        copia.setTipoInstalacao(origem.getTipoInstalacao());
        return copia;
    }
}
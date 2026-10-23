
package br.com.demandwise.service;

import br.com.demandwise.projeto.ProjetoPreValidado;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProjetoPreValidadoService {

    private final List<ProjetoPreValidado> projetos = new ArrayList<>();
    private final AtomicLong proximoId = new AtomicLong(1);

    public ProjetoPreValidado salvar(ProjetoPreValidado projeto) {
        ProjetoPreValidado projetoSalvo = new ProjetoPreValidado(
                proximoId.getAndIncrement(),
                "Pré-validado",
                projeto.edificacao(),
                projeto.resultadoDemanda()
        );

        projetos.add(projetoSalvo);
        return projetoSalvo;
    }

    public List<ProjetoPreValidado> listarTodos() {
        return List.copyOf(projetos);
    }

    public ProjetoPreValidado buscarPorId(Long id) {
        return projetos.stream()
                .filter(projeto -> projeto.id().equals(id))
                .findFirst()
                .orElse(null);
    }
}

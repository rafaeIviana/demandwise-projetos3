package br.com.demandwise.service;

import br.com.demandwise.model.MotobombaIncendio;
import org.springframework.stereotype.Service;

/** Guards the calculation until the applicable fire-pump rule/table has been confirmed. */
@Service
public class DemandaMotobombaService {
    public void validarRegraDisponivel(MotobombaIncendio motobomba) {
        if (motobomba != null && motobomba.presente()) {
            throw new IllegalArgumentException("Não foi calculada a demanda: a regra normativa da "
                    + "motobomba de incêndio ainda precisa ser confirmada.");
        }
    }
}

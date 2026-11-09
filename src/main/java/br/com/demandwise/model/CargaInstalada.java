package br.com.demandwise.model;

/** One installed load item; its category is defined by the containing list in CargasCondominio. */
public record CargaInstalada(
        String identificacao,
        Potencia potencia,
        int quantidade
) {
    public CargaInstalada {
        if (potencia == null) {
            throw new IllegalArgumentException("A potência da carga é obrigatória.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade da carga deve ser maior que zero.");
        }
    }
}

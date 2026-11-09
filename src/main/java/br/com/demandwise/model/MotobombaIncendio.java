package br.com.demandwise.model;

/** Fire-pump input; demand is not accepted here and must be derived by a later calculation. */
public record MotobombaIncendio(
        boolean presente,
        String identificacao,
        Potencia potenciaNominal
) {
    public MotobombaIncendio {
        if (presente && potenciaNominal == null) {
            throw new IllegalArgumentException("A potência nominal da motobomba é obrigatória.");
        }
        if (!presente && potenciaNominal != null) {
            throw new IllegalArgumentException("Uma motobomba ausente não pode ter potência informada.");
        }
    }

    public static MotobombaIncendio ausente() {
        return new MotobombaIncendio(false, null, null);
    }
}

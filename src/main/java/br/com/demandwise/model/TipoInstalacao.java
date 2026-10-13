package br.com.demandwise.model;

public enum TipoInstalacao {

    RESIDENCIAL("Residencial", true),
    COMERCIAL("Comercial", false),
    ALTO_RISCO("Local de alto risco", false);

    private final String descricao;
    private final boolean disponivel;

    TipoInstalacao(String descricao, boolean disponivel) {
        this.descricao = descricao;
        this.disponivel = disponivel;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isDisponivel() {
        return disponivel;
    }
}
package br.com.demandwise.model;

import java.util.List;

/** Installed condominium loads, separated by category for later normative calculations. */
public record CargasCondominio(
        List<CargaInstalada> iluminacao,
        List<CargaInstalada> tomadasUsoGeral,
        List<CargaInstalada> arCondicionado,
        List<CargaInstalada> motores,
        List<CargaInstalada> equipamentosEspeciais,
        List<CargaInstalada> bombasHidromassagem
) {
    public CargasCondominio {
        iluminacao = listaImutavel(iluminacao);
        tomadasUsoGeral = listaImutavel(tomadasUsoGeral);
        arCondicionado = listaImutavel(arCondicionado);
        motores = listaImutavel(motores);
        equipamentosEspeciais = listaImutavel(equipamentosEspeciais);
        bombasHidromassagem = listaImutavel(bombasHidromassagem);
    }

    public CargasCondominio() {
        this(List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private static List<CargaInstalada> listaImutavel(List<CargaInstalada> cargas) {
        return cargas == null ? List.of() : List.copyOf(cargas);
    }
}

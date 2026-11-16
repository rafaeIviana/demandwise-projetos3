package br.com.demandwise.model;

import jakarta.validation.Valid;

import java.util.List;
import java.util.ArrayList;

public class Edificacao {

  @Valid
  private List<Apartamento> apartamentos;

  private List<GrupoResidencial> gruposResidenciais = new ArrayList<>();
  private CargasCondominio cargasCondominio = new CargasCondominio();
  private MotobombaIncendio motobombaIncendio = MotobombaIncendio.ausente();

  private double potenciaIluminacao;
  private double potenciaTomadas;
  // Legacy field kept for compatibility with the current calculation and API payloads.
  private List<Double> potenciasMotores;
  private TipoInstalacao tipoInstalacao = TipoInstalacao.RESIDENCIAL;

  public Edificacao() {
  }

  public Edificacao(List<Apartamento> apartamentos) {
    this.apartamentos = apartamentos;
  }

  public List<Apartamento> getApartamentos() {
    return apartamentos;
  }

  public void setApartamentos(List<Apartamento> apartamentos) {
    this.apartamentos = apartamentos;
  }

  public List<GrupoResidencial> getGruposResidenciais() {
    return List.copyOf(gruposResidenciais);
  }

  public void setGruposResidenciais(List<GrupoResidencial> gruposResidenciais) {
    this.gruposResidenciais = gruposResidenciais == null
            ? new ArrayList<>() : new ArrayList<>(gruposResidenciais);
  }

  public CargasCondominio getCargasCondominio() {
    return cargasCondominio;
  }

  public void setCargasCondominio(CargasCondominio cargasCondominio) {
    this.cargasCondominio = cargasCondominio == null ? new CargasCondominio() : cargasCondominio;
  }

  public MotobombaIncendio getMotobombaIncendio() {
    return motobombaIncendio;
  }

  public void setMotobombaIncendio(MotobombaIncendio motobombaIncendio) {
    this.motobombaIncendio = motobombaIncendio == null
            ? MotobombaIncendio.ausente() : motobombaIncendio;
  }

  public double getPotenciaIluminacao() {
    return potenciaIluminacao;
  }

  public void setPotenciaIluminacao(double potenciaIluminacao) {
    this.potenciaIluminacao = potenciaIluminacao;
  }

  public double getPotenciaTomadas() {
    return potenciaTomadas;
  }

  public void setPotenciaTomadas(double potenciaTomadas) {
    this.potenciaTomadas = potenciaTomadas;
  }

  public List<Double> getPotenciasMotores() {
    return potenciasMotores;
  }

  public void setPotenciasMotores(List<Double> potenciasMotores) {
    this.potenciasMotores = potenciasMotores;
  }

  public int getNumeroApartamentos() {
    return apartamentos == null ? 0 : apartamentos.size();
  }

  public int getQuantidadeUnidadesResidenciais() {
    if (apartamentos != null && !apartamentos.isEmpty()) {
      return apartamentos.size();
    }
    return gruposResidenciais.stream().mapToInt(GrupoResidencial::quantidade).sum();
  }

  public boolean isRepresentacaoResidencialAgrupada() {
    return (apartamentos == null || apartamentos.isEmpty()) && !gruposResidenciais.isEmpty();
  }

  public TipoInstalacao getTipoInstalacao() {
    return tipoInstalacao;
  }
public void setTipoInstalacao(TipoInstalacao tipoInstalacao) {
    this.tipoInstalacao = tipoInstalacao == null ? TipoInstalacao.RESIDENCIAL : tipoInstalacao;
 }


}


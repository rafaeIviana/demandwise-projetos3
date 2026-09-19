package br.com.demandwise.model;

import jakarta.validation.Valid;

import java.util.List;

public class Edificacao {

  @Valid
  private List<Apartamento> apartamentos;

  private double potenciaIluminacao;
  private double potenciaTomadas;
  private List<Double> potenciasMotores;

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
}


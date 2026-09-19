package br.com.demandwise.model;

import jakarta.validation.Valid;

import java.util.List;
public class Edificacao {

  @Valid
  private List<Apartamento> apartamentos;

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

  public int getNumeroApartamentos() {
    return apartamentos == null ? 0 : apartamentos.size();
  }
}

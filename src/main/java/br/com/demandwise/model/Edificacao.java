package br.com.demandwise.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

public class Edificacao {

  @Valid
  @Size(min = 2, message = "Projeto MUC deve ter no mínimo 2 unidades consumidoras (norma NDU-PE-02)")
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
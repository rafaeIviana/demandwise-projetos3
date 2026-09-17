package br.com.demandwise.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public class Apartamento {

  @DecimalMin(value = "20.0", message = "Área útil deve ser no mínimo 20 m² (norma NDU-PE-01)")
  @DecimalMax(value = "500.0", message = "Área útil deve ser no máximo 500 m² (norma NDU-PE-01)")
  private double areaUtil;

  public Apartamento() {
  }

  public Apartamento(double areaUtil) {
    this.areaUtil = areaUtil;
  }

  public double getAreaUtil() {
    return areaUtil;
  }

  public void setAreaUtil(double areaUtil) {
    this.areaUtil = areaUtil;
  }
}
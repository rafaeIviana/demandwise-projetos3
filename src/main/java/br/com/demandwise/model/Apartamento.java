package br.com.demandwise.model;

import jakarta.validation.constraints.AssertTrue;

public class Apartamento {

  private double areaUtil;
  private double potenciaInstalada;
  private double potenciaRecargaVe;
  private boolean possuiEstudoDeRede;

  public Apartamento() {
  }

  public Apartamento(double areaUtil, double potenciaInstalada, double potenciaRecargaVe, boolean possuiEstudoDeRede) {
    this.areaUtil = areaUtil;
    this.potenciaInstalada = potenciaInstalada;
    this.potenciaRecargaVe = potenciaRecargaVe;
    this.possuiEstudoDeRede = possuiEstudoDeRede;
  }

  @AssertTrue(message = "Potência total da unidade (incluindo recarga de veículo elétrico) ultrapassa 20 kW — norma DIS-NOR-030, item 6.26.4, exige estudo de rede de distribuição antes da aprovação")
  public boolean isPotenciaDentroDoLimiteSemEstudo() {
    return (potenciaInstalada + potenciaRecargaVe) <= 20.0 || possuiEstudoDeRede;
  }

  public double getAreaUtil() {
    return areaUtil;
  }

  public void setAreaUtil(double areaUtil) {
    this.areaUtil = areaUtil;
  }

  public double getPotenciaInstalada() {
    return potenciaInstalada;
  }

  public void setPotenciaInstalada(double potenciaInstalada) {
    this.potenciaInstalada = potenciaInstalada;
  }

  public double getPotenciaRecargaVe() {
    return potenciaRecargaVe;
  }

  public void setPotenciaRecargaVe(double potenciaRecargaVe) {
    this.potenciaRecargaVe = potenciaRecargaVe;
  }

  public boolean isPossuiEstudoDeRede() {
    return possuiEstudoDeRede;
  }

  public void setPossuiEstudoDeRede(boolean possuiEstudoDeRede) {
    this.possuiEstudoDeRede = possuiEstudoDeRede;
  }
}

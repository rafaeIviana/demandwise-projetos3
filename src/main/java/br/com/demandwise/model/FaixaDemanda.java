package br.com.demandwise.model;

public class FaixaDemanda {

    private double areaMaxima;
    private double demanda;

    public FaixaDemanda(double areaMaxima, double demanda) {
        this.areaMaxima = areaMaxima;
        this.demanda = demanda;
    }

    public double getAreaMaxima() {
        return areaMaxima;
    }

    public double getDemanda() {
        return demanda;
    }
}
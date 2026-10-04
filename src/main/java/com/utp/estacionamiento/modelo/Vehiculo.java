package com.utp.estacionamiento.modelo;

public class Vehiculo {
    private int idVehiculo;
    private String placa;
    private String marca;
    private String modelo;
    private String color;
    private int idTipo;
    private String nombreTipo; // opcional: para mostrar en listados sin otro JOIN manual

    public Vehiculo() {}

    public Vehiculo(int idVehiculo, String placa, String marca, String modelo, String color, int idTipo) {
        this.idVehiculo = idVehiculo;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.color = color;
        this.idTipo = idTipo;
    }

    public int getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(int idVehiculo) { this.idVehiculo = idVehiculo; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public int getIdTipo() { return idTipo; }
    public void setIdTipo(int idTipo) { this.idTipo = idTipo; }
    public String getNombreTipo() { return nombreTipo; }
    public void setNombreTipo(String nombreTipo) { this.nombreTipo = nombreTipo; }

    @Override
    public String toString() {
        return placa + " - " + marca + " " + modelo;
    }
}

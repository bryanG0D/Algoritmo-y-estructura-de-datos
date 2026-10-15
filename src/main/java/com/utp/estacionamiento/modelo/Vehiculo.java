package com.utp.estacionamiento.modelo;

public class Vehiculo {
    private int idVehiculo;
    private String placa;
    private String marca;
    private int idTipo;
    private String nombreTipo; // opcional: para mostrar en listados sin otro JOIN manual

    public Vehiculo() {}

    public Vehiculo(int idVehiculo, String placa, String marca, int idTipo) {
        this.idVehiculo = idVehiculo;
        this.placa = placa;
        this.marca = marca;
        this.idTipo = idTipo;
    }

    public int getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(int idVehiculo) { this.idVehiculo = idVehiculo; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public int getIdTipo() { return idTipo; }
    public void setIdTipo(int idTipo) { this.idTipo = idTipo; }
    public String getNombreTipo() { return nombreTipo; }
    public void setNombreTipo(String nombreTipo) { this.nombreTipo = nombreTipo; }

    @Override
    public String toString() {
        return placa + " - " + marca;
    }
}

package com.utp.estacionamiento.dao;

public class ResultadoEntrada {
    private final Integer idTicket;
    private final Integer idEspacio;
    private final int idVehiculo;

    public ResultadoEntrada(Integer idTicket, Integer idEspacio, int idVehiculo) {
        this.idTicket = idTicket;
        this.idEspacio = idEspacio;
        this.idVehiculo = idVehiculo;
    }

    public Integer getIdTicket() { return idTicket; }
    public Integer getIdEspacio() { return idEspacio; }
    public int getIdVehiculo() { return idVehiculo; }
    public boolean fueAsignado() { return idTicket != null; }
}

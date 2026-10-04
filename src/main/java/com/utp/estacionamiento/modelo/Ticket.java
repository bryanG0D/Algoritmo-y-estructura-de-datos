package com.utp.estacionamiento.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Ticket {
    private int idTicket;
    private int idVehiculo;
    private int idEspacio;
    private int idUsuarioRegistro;
    private LocalDateTime fechaHoraEntrada;
    private LocalDateTime fechaHoraSalida;
    private String estadoTicket; // ACTIVO, FINALIZADO
    private BigDecimal montoTotal;

    public Ticket() {}

    public int getIdTicket() { return idTicket; }
    public void setIdTicket(int idTicket) { this.idTicket = idTicket; }
    public int getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(int idVehiculo) { this.idVehiculo = idVehiculo; }
    public int getIdEspacio() { return idEspacio; }
    public void setIdEspacio(int idEspacio) { this.idEspacio = idEspacio; }
    public int getIdUsuarioRegistro() { return idUsuarioRegistro; }
    public void setIdUsuarioRegistro(int idUsuarioRegistro) { this.idUsuarioRegistro = idUsuarioRegistro; }
    public LocalDateTime getFechaHoraEntrada() { return fechaHoraEntrada; }
    public void setFechaHoraEntrada(LocalDateTime fechaHoraEntrada) { this.fechaHoraEntrada = fechaHoraEntrada; }
    public LocalDateTime getFechaHoraSalida() { return fechaHoraSalida; }
    public void setFechaHoraSalida(LocalDateTime fechaHoraSalida) { this.fechaHoraSalida = fechaHoraSalida; }
    public String getEstadoTicket() { return estadoTicket; }
    public void setEstadoTicket(String estadoTicket) { this.estadoTicket = estadoTicket; }
    public BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }
}

package com.utp.estacionamiento.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pago {
    private int idPago;
    private int idTicket;
    private BigDecimal monto;
    private String metodoPago;
    private LocalDateTime fechaHoraPago;
    private int idUsuarioCobro;

    public Pago() {}

    public int getIdPago() { return idPago; }
    public void setIdPago(int idPago) { this.idPago = idPago; }
    public int getIdTicket() { return idTicket; }
    public void setIdTicket(int idTicket) { this.idTicket = idTicket; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public LocalDateTime getFechaHoraPago() { return fechaHoraPago; }
    public void setFechaHoraPago(LocalDateTime fechaHoraPago) { this.fechaHoraPago = fechaHoraPago; }
    public int getIdUsuarioCobro() { return idUsuarioCobro; }
    public void setIdUsuarioCobro(int idUsuarioCobro) { this.idUsuarioCobro = idUsuarioCobro; }
}

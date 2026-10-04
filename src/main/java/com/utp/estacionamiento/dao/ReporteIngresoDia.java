package com.utp.estacionamiento.dao;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReporteIngresoDia {
    private final LocalDate fecha;
    private final int cantidadTickets;
    private final BigDecimal ingresoTotal;

    public ReporteIngresoDia(LocalDate fecha, int cantidadTickets, BigDecimal ingresoTotal) {
        this.fecha = fecha;
        this.cantidadTickets = cantidadTickets;
        this.ingresoTotal = ingresoTotal;
    }

    public LocalDate getFecha() { return fecha; }
    public int getCantidadTickets() { return cantidadTickets; }
    public BigDecimal getIngresoTotal() { return ingresoTotal; }
}

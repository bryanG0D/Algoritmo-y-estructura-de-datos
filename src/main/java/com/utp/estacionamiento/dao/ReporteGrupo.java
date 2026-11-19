package com.utp.estacionamiento.dao;

import java.math.BigDecimal;

/** Fila de un reporte agrupado (por tipo de vehiculo o por metodo de pago). */
public class ReporteGrupo {
    private final String nombre;
    private final int cantidad;
    private final BigDecimal ingreso;

    public ReporteGrupo(String nombre, int cantidad, BigDecimal ingreso) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.ingreso = ingreso;
    }

    public String getNombre() { return nombre; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getIngreso() { return ingreso; }
}

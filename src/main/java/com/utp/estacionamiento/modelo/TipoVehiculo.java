package com.utp.estacionamiento.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TipoVehiculo {
    private int idTipo;
    private String nombre;
    private String descripcion;
    private BigDecimal precioHora;
    private BigDecimal precioFraccion;
    private LocalDate vigenteDesde;

    public TipoVehiculo() {}

    public TipoVehiculo(int idTipo, String nombre, String descripcion,
                         BigDecimal precioHora, BigDecimal precioFraccion, LocalDate vigenteDesde) {
        this.idTipo = idTipo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioHora = precioHora;
        this.precioFraccion = precioFraccion;
        this.vigenteDesde = vigenteDesde;
    }

    public int getIdTipo() { return idTipo; }
    public void setIdTipo(int idTipo) { this.idTipo = idTipo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getPrecioHora() { return precioHora; }
    public void setPrecioHora(BigDecimal precioHora) { this.precioHora = precioHora; }
    public BigDecimal getPrecioFraccion() { return precioFraccion; }
    public void setPrecioFraccion(BigDecimal precioFraccion) { this.precioFraccion = precioFraccion; }
    public LocalDate getVigenteDesde() { return vigenteDesde; }
    public void setVigenteDesde(LocalDate vigenteDesde) { this.vigenteDesde = vigenteDesde; }

    @Override
    public String toString() {
        return nombre;
    }
}

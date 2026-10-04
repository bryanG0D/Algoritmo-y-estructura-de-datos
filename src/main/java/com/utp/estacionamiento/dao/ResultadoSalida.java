package com.utp.estacionamiento.dao;

import java.math.BigDecimal;

public class ResultadoSalida {
    private final BigDecimal monto;
    private final int idEspacioLiberado;

    public ResultadoSalida(BigDecimal monto, int idEspacioLiberado) {
        this.monto = monto;
        this.idEspacioLiberado = idEspacioLiberado;
    }

    public BigDecimal getMonto() { return monto; }
    public int getIdEspacioLiberado() { return idEspacioLiberado; }
}

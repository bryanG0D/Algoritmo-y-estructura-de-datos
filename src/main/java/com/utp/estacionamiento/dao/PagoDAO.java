package com.utp.estacionamiento.dao;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface PagoDAO {
    List<ReporteIngresoDia> reporteIngresosPorRango(LocalDate inicio, LocalDate fin) throws SQLException;
}

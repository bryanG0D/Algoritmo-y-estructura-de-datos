package com.utp.estacionamiento.dao;

import com.utp.estacionamiento.conexion.ConexionBD;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PagoDAOImpl implements PagoDAO {

    @Override
    public List<ReporteIngresoDia> reporteIngresosPorRango(LocalDate inicio, LocalDate fin) throws SQLException {
        List<ReporteIngresoDia> lista = new ArrayList<>();
        String sql = "{CALL sp_reporte_ingresos_por_rango(?, ?)}";
        try (Connection con = ConexionBD.obtenerConexion();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setDate(1, Date.valueOf(inicio));
            cs.setDate(2, Date.valueOf(fin));
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ReporteIngresoDia(
                            rs.getDate("fecha").toLocalDate(),
                            rs.getInt("cantidad_tickets"),
                            rs.getBigDecimal("ingreso_total")
                    ));
                }
            }
        }
        return lista;
    }
}

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

    @Override
    public List<ReporteGrupo> reportePorTipo(LocalDate inicio, LocalDate fin) throws SQLException {
        return reporteAgrupado("{CALL sp_reporte_por_tipo(?, ?)}", inicio, fin);
    }

    @Override
    public List<ReporteGrupo> reportePorMetodoPago(LocalDate inicio, LocalDate fin) throws SQLException {
        return reporteAgrupado("{CALL sp_reporte_por_metodo_pago(?, ?)}", inicio, fin);
    }

    /** Ambos procedimientos devuelven las columnas nombre, cantidad e ingreso. */
    private List<ReporteGrupo> reporteAgrupado(String sql, LocalDate inicio, LocalDate fin) throws SQLException {
        List<ReporteGrupo> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setDate(1, Date.valueOf(inicio));
            cs.setDate(2, Date.valueOf(fin));
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ReporteGrupo(rs.getString("nombre"), rs.getInt("cantidad"), rs.getBigDecimal("ingreso")));
                }
            }
        }
        return lista;
    }
}

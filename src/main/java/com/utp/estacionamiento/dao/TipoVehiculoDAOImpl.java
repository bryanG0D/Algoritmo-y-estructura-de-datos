package com.utp.estacionamiento.dao;

import com.utp.estacionamiento.conexion.ConexionBD;
import com.utp.estacionamiento.modelo.TipoVehiculo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TipoVehiculoDAOImpl implements TipoVehiculoDAO {

    @Override
    public List<TipoVehiculo> listarTodos() throws SQLException {
        List<TipoVehiculo> lista = new ArrayList<>();
        String sql = "SELECT * FROM tipo_vehiculo ORDER BY nombre";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public TipoVehiculo buscarPorId(int idTipo) throws SQLException {
        String sql = "SELECT * FROM tipo_vehiculo WHERE id_tipo=?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idTipo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    private TipoVehiculo mapear(ResultSet rs) throws SQLException {
        TipoVehiculo t = new TipoVehiculo();
        t.setIdTipo(rs.getInt("id_tipo"));
        t.setNombre(rs.getString("nombre"));
        t.setDescripcion(rs.getString("descripcion"));
        t.setPrecioHora(rs.getBigDecimal("precio_hora"));
        t.setPrecioFraccion(rs.getBigDecimal("precio_fraccion"));
        Date fecha = rs.getDate("vigente_desde");
        t.setVigenteDesde(fecha != null ? fecha.toLocalDate() : null);
        return t;
    }
}

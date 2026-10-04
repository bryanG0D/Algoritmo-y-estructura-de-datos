package com.utp.estacionamiento.dao;

import com.utp.estacionamiento.conexion.ConexionBD;
import com.utp.estacionamiento.modelo.EspacioEstacionamiento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EspacioEstacionamientoDAOImpl implements EspacioEstacionamientoDAO {

    @Override
    public List<EspacioEstacionamiento> listarTodos() throws SQLException {
        List<EspacioEstacionamiento> lista = new ArrayList<>();
        String sql = "SELECT * FROM espacio_estacionamiento ORDER BY fila, columna";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                EspacioEstacionamiento e = new EspacioEstacionamiento();
                e.setIdEspacio(rs.getInt("id_espacio"));
                e.setIdZona(rs.getInt("id_zona"));
                e.setNumeroEspacio(rs.getString("numero_espacio"));
                e.setFila(rs.getInt("fila"));
                e.setColumna(rs.getInt("columna"));
                e.setEstado(rs.getString("estado"));
                e.setIdTipoPermitido(rs.getInt("id_tipo_permitido"));
                lista.add(e);
            }
        }
        return lista;
    }

    @Override
    public void actualizarEstado(int idEspacio, String estado) throws SQLException {
        String sql = "UPDATE espacio_estacionamiento SET estado=? WHERE id_espacio=?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, idEspacio);
            ps.executeUpdate();
        }
    }
}

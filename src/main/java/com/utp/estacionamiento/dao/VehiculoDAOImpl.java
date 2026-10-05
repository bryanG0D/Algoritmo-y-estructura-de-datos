package com.utp.estacionamiento.dao;

import com.utp.estacionamiento.conexion.ConexionBD;
import com.utp.estacionamiento.modelo.Vehiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class VehiculoDAOImpl implements VehiculoDAO {

    @Override
    public void insertar(Vehiculo vehiculo) throws SQLException {
        String sql = "INSERT INTO vehiculo (placa, marca, id_tipo) VALUES (?, ?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, vehiculo.getPlaca());
            ps.setString(2, vehiculo.getMarca());
            ps.setInt(3, vehiculo.getIdTipo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    vehiculo.setIdVehiculo(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void actualizar(Vehiculo vehiculo) throws SQLException {
        String sql = "UPDATE vehiculo SET placa=?, marca=?, id_tipo=? WHERE id_vehiculo=?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, vehiculo.getPlaca());
            ps.setString(2, vehiculo.getMarca());
            ps.setInt(3, vehiculo.getIdTipo());
            ps.setInt(4, vehiculo.getIdVehiculo());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(int idVehiculo) throws SQLException {
        String sql = "DELETE FROM vehiculo WHERE id_vehiculo=?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVehiculo);
            ps.executeUpdate();
        }
    }

    @Override
    public Vehiculo buscarPorId(int idVehiculo) throws SQLException {
        String sql = "SELECT * FROM vehiculo WHERE id_vehiculo=?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVehiculo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public Vehiculo buscarPorPlaca(String placa) throws SQLException {
        String sql = "SELECT * FROM vehiculo WHERE placa=?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, placa);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Vehiculo> listarTodos() throws SQLException {
        List<Vehiculo> lista = new ArrayList<>();
        String sql = "SELECT v.*, t.nombre AS nombre_tipo FROM vehiculo v " +
                     "JOIN tipo_vehiculo t ON t.id_tipo = v.id_tipo ORDER BY v.id_vehiculo";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Vehiculo v = mapear(rs);
                v.setNombreTipo(rs.getString("nombre_tipo"));
                lista.add(v);
            }
        }
        return lista;
    }

    private Vehiculo mapear(ResultSet rs) throws SQLException {
        Vehiculo v = new Vehiculo();
        v.setIdVehiculo(rs.getInt("id_vehiculo"));
        v.setPlaca(rs.getString("placa"));
        v.setMarca(rs.getString("marca"));
        v.setIdTipo(rs.getInt("id_tipo"));
        return v;
    }
}

package com.utp.estacionamiento.dao;

import com.utp.estacionamiento.conexion.ConexionBD;
import com.utp.estacionamiento.modelo.Ticket;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion que delega la logica transaccional a los
 * procedimientos almacenados: sp_registrar_entrada (con el espacio
 * ya elegido por la Matriz en Java) y sp_registrar_salida.
 */
public class TicketDAOImpl implements TicketDAO {

    @Override
    public ResultadoEntrada registrarEntrada(String placa, String marca,
                                              int idTipo, int idUsuario, int idEspacio) throws SQLException {
        String sql = "{CALL sp_registrar_entrada(?, ?, ?, ?, ?, ?)}";
        try (Connection con = ConexionBD.obtenerConexion();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, placa);
            cs.setString(2, marca);
            cs.setInt(3, idTipo);
            cs.setInt(4, idUsuario);
            cs.setInt(5, idEspacio);
            cs.registerOutParameter(6, Types.INTEGER);
            cs.execute();

            int idTicketOut = cs.getInt(6);
            Integer idTicket = cs.wasNull() ? null : idTicketOut;

            int idVehiculo = obtenerIdVehiculoPorPlaca(con, placa);
            return new ResultadoEntrada(idTicket, idTicket == null ? null : idEspacio, idVehiculo);
        }
    }

    private int obtenerIdVehiculoPorPlaca(Connection con, String placa) throws SQLException {
        String sql = "SELECT id_vehiculo FROM vehiculo WHERE placa=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, placa);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    @Override
    public ResultadoSalida registrarSalida(int idTicket, int idUsuarioCobro, String metodoPago) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion()) {
            int idEspacio;
            String sqlEspacio = "SELECT id_espacio FROM ticket WHERE id_ticket=?";
            try (PreparedStatement ps = con.prepareStatement(sqlEspacio)) {
                ps.setInt(1, idTicket);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Ticket no encontrado: " + idTicket);
                    }
                    idEspacio = rs.getInt("id_espacio");
                }
            }

            String sqlProc = "{CALL sp_registrar_salida(?, ?, ?, ?)}";
            try (CallableStatement cs = con.prepareCall(sqlProc)) {
                cs.setInt(1, idTicket);
                cs.setInt(2, idUsuarioCobro);
                cs.setString(3, metodoPago);
                cs.registerOutParameter(4, Types.DECIMAL);
                cs.execute();
                BigDecimal monto = cs.getBigDecimal(4);
                return new ResultadoSalida(monto, idEspacio);
            }
        }
    }

    @Override
    public Ticket buscarPorId(int idTicket) throws SQLException {
        String sql = "SELECT * FROM ticket WHERE id_ticket=?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idTicket);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Ticket> listarActivos() throws SQLException {
        return listarPorEstado("ACTIVO");
    }

    @Override
    public List<Ticket> listarTodos() throws SQLException {
        List<Ticket> lista = new ArrayList<>();
        String sql = "SELECT * FROM ticket ORDER BY fecha_hora_entrada DESC";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private List<Ticket> listarPorEstado(String estado) throws SQLException {
        List<Ticket> lista = new ArrayList<>();
        String sql = "SELECT * FROM ticket WHERE estado_ticket=? ORDER BY fecha_hora_entrada";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private Ticket mapear(ResultSet rs) throws SQLException {
        Ticket t = new Ticket();
        t.setIdTicket(rs.getInt("id_ticket"));
        t.setIdVehiculo(rs.getInt("id_vehiculo"));
        t.setIdEspacio(rs.getInt("id_espacio"));
        t.setIdUsuarioRegistro(rs.getInt("id_usuario_registro"));
        Timestamp entrada = rs.getTimestamp("fecha_hora_entrada");
        t.setFechaHoraEntrada(entrada != null ? entrada.toLocalDateTime() : null);
        Timestamp salida = rs.getTimestamp("fecha_hora_salida");
        t.setFechaHoraSalida(salida != null ? salida.toLocalDateTime() : null);
        t.setEstadoTicket(rs.getString("estado_ticket"));
        t.setMontoTotal(rs.getBigDecimal("monto_total"));
        return t;
    }
}

package com.utp.estacionamiento.dao;

import com.utp.estacionamiento.modelo.Ticket;

import java.sql.SQLException;
import java.util.List;

public interface TicketDAO {
    ResultadoEntrada registrarEntrada(String placa, String marca,
                                       int idTipo, int idUsuario, int idEspacio) throws SQLException;
    ResultadoSalida registrarSalida(int idTicket, int idUsuarioCobro, String metodoPago) throws SQLException;
    Ticket buscarPorId(int idTicket) throws SQLException;
    List<Ticket> listarActivos() throws SQLException;
    List<Ticket> listarTodos() throws SQLException;
}

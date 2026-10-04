package com.utp.estacionamiento.dao;

import com.utp.estacionamiento.modelo.EspacioEstacionamiento;

import java.sql.SQLException;
import java.util.List;

public interface EspacioEstacionamientoDAO {
    List<EspacioEstacionamiento> listarTodos() throws SQLException;
    void actualizarEstado(int idEspacio, String estado) throws SQLException;
}

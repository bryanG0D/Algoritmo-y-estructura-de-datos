package com.utp.estacionamiento.dao;

import com.utp.estacionamiento.modelo.TipoVehiculo;

import java.sql.SQLException;
import java.util.List;

public interface TipoVehiculoDAO {
    List<TipoVehiculo> listarTodos() throws SQLException;
    TipoVehiculo buscarPorId(int idTipo) throws SQLException;
}

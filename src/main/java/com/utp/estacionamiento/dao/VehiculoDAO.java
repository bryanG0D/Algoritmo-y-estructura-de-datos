package com.utp.estacionamiento.dao;

import com.utp.estacionamiento.modelo.Vehiculo;

import java.sql.SQLException;
import java.util.List;

public interface VehiculoDAO {
    void insertar(Vehiculo vehiculo) throws SQLException;
    void actualizar(Vehiculo vehiculo) throws SQLException;
    void eliminar(int idVehiculo) throws SQLException;
    Vehiculo buscarPorId(int idVehiculo) throws SQLException;
    Vehiculo buscarPorPlaca(String placa) throws SQLException;
    List<Vehiculo> listarTodos() throws SQLException;
}

package com.utp.estacionamiento.dao;

import com.utp.estacionamiento.modelo.Usuario;

import java.sql.SQLException;
import java.util.List;

public interface UsuarioDAO {
    Usuario buscarPorLogin(String login) throws SQLException;
    Usuario buscarPorId(int idUsuario) throws SQLException;
    List<Usuario> listarTodos() throws SQLException;
}

package com.utp.estacionamiento.servlet;

import com.google.gson.Gson;
import com.utp.estacionamiento.util.GsonProvider;
import com.utp.estacionamiento.dao.TipoVehiculoDAO;
import com.utp.estacionamiento.dao.TipoVehiculoDAOImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TipoVehiculoServlet extends HttpServlet {

    private final TipoVehiculoDAO tipoVehiculoDAO = new TipoVehiculoDAOImpl();
    private final Gson gson = GsonProvider.gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> respuesta = new HashMap<>();
        try {
            respuesta.put("tipos", tipoVehiculoDAO.listarTodos());
        } catch (SQLException e) {
            respuesta.put("error", e.getMessage());
        }
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(gson.toJson(respuesta));
    }
}

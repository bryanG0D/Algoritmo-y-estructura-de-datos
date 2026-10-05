package com.utp.estacionamiento.servlet;

import com.google.gson.Gson;
import com.utp.estacionamiento.util.GsonProvider;
import com.utp.estacionamiento.dao.VehiculoDAO;
import com.utp.estacionamiento.dao.VehiculoDAOImpl;
import com.utp.estacionamiento.modelo.Vehiculo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/** Demuestra el CRUD completo (Crear, Leer, Actualizar, Eliminar) con patron DAO. */
public class VehiculoServlet extends HttpServlet {

    private final VehiculoDAO vehiculoDAO = new VehiculoDAOImpl();
    private final Gson gson = GsonProvider.gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> respuesta = new HashMap<>();
        try {
            respuesta.put("vehiculos", vehiculoDAO.listarTodos());
        } catch (SQLException e) {
            respuesta.put("error", e.getMessage());
        }
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(gson.toJson(respuesta));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> respuesta = new HashMap<>();
        String accion = req.getParameter("accion");

        try {
            switch (accion) {
                case "crear": {
                    Vehiculo v = new Vehiculo();
                    v.setPlaca(req.getParameter("placa").trim().toUpperCase());
                    v.setMarca(opcional(req.getParameter("marca")));
                    v.setModelo(opcional(req.getParameter("modelo")));
                    v.setColor(opcional(req.getParameter("color")));
                    v.setIdTipo(Integer.parseInt(req.getParameter("idTipo")));
                    vehiculoDAO.insertar(v);
                    respuesta.put("exito", true);
                    break;
                }
                case "actualizar": {
                    Vehiculo v = new Vehiculo();
                    v.setIdVehiculo(Integer.parseInt(req.getParameter("idVehiculo")));
                    v.setPlaca(req.getParameter("placa").trim().toUpperCase());
                    v.setMarca(opcional(req.getParameter("marca")));
                    v.setModelo(opcional(req.getParameter("modelo")));
                    v.setColor(opcional(req.getParameter("color")));
                    v.setIdTipo(Integer.parseInt(req.getParameter("idTipo")));
                    vehiculoDAO.actualizar(v);
                    respuesta.put("exito", true);
                    break;
                }
                case "eliminar": {
                    int id = Integer.parseInt(req.getParameter("idVehiculo"));
                    vehiculoDAO.eliminar(id);
                    respuesta.put("exito", true);
                    break;
                }
                default:
                    respuesta.put("exito", false);
                    respuesta.put("mensaje", "Accion no reconocida: " + accion);
            }
        } catch (SQLException e) {
            respuesta.put("exito", false);
            respuesta.put("mensaje", "Error de base de datos: " + e.getMessage());
        }

        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(gson.toJson(respuesta));
    }

    /** Marca, modelo y color son opcionales: si llegan vacios se guardan como NULL. */
    private static String opcional(String valor) {
        return (valor == null || valor.trim().isEmpty()) ? null : valor.trim();
    }
}

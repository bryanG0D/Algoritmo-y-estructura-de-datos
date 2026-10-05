package com.utp.estacionamiento.servlet;

import com.google.gson.Gson;
import com.utp.estacionamiento.util.GsonProvider;
import com.utp.estacionamiento.modelo.Usuario;
import com.utp.estacionamiento.servicio.EstacionamientoService;
import com.utp.estacionamiento.util.AppContextListener;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class EntradaServlet extends HttpServlet {

    private final Gson gson = GsonProvider.gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> respuesta = new HashMap<>();
        try {
            EstacionamientoService servicio = (EstacionamientoService)
                    getServletContext().getAttribute(AppContextListener.ATRIBUTO_SERVICIO);

            String placa = req.getParameter("placa").trim().toUpperCase();
            String marca = opcional(req.getParameter("marca"));
            int idTipo = Integer.parseInt(req.getParameter("idTipo"));
            int idUsuario = obtenerIdUsuarioSesion(req);

            EstacionamientoService.ResultadoRegistro r =
                    servicio.registrarEntrada(placa, marca, idTipo, idUsuario);

            respuesta.put("asignado", r.asignado);
            respuesta.put("idTicket", r.idTicket);
            respuesta.put("numeroEspacio", r.numeroEspacio);
            respuesta.put("mensaje", r.mensaje);
        } catch (SQLException e) {
            respuesta.put("error", "Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            respuesta.put("error", "Datos invalidos: " + e.getMessage());
        }

        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(gson.toJson(respuesta));
    }

    private int obtenerIdUsuarioSesion(HttpServletRequest req) {
        HttpSession sesion = req.getSession(false);
        if (sesion != null && sesion.getAttribute("usuario") != null) {
            return ((Usuario) sesion.getAttribute("usuario")).getIdUsuario();
        }
        return 1; // usuario por defecto si no hay sesion activa
    }

    /** La marca es opcional: si llega vacia se guarda como NULL. */
    private static String opcional(String valor) {
        return (valor == null || valor.trim().isEmpty()) ? null : valor.trim();
    }
}

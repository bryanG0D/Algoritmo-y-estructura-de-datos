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

public class SalidaServlet extends HttpServlet {

    private final Gson gson = GsonProvider.gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> respuesta = new HashMap<>();
        try {
            EstacionamientoService servicio = (EstacionamientoService)
                    getServletContext().getAttribute(AppContextListener.ATRIBUTO_SERVICIO);

            int idTicket = Integer.parseInt(req.getParameter("idTicket"));
            String metodoPago = req.getParameter("metodoPago");
            int idUsuario = obtenerIdUsuarioSesion(req);

            EstacionamientoService.ResultadoCobro r = servicio.registrarSalida(idTicket, idUsuario, metodoPago);

            respuesta.put("monto", r.monto);
            respuesta.put("numeroEspacioLiberado", r.numeroEspacioLiberado);
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
        return 1;
    }
}

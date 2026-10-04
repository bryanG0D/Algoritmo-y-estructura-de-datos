package com.utp.estacionamiento.servlet;

import com.google.gson.Gson;
import com.utp.estacionamiento.util.GsonProvider;
import com.utp.estacionamiento.modelo.Ticket;
import com.utp.estacionamiento.modelo.Vehiculo;
import com.utp.estacionamiento.servicio.EstacionamientoService;
import com.utp.estacionamiento.util.AppContextListener;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class BusquedaServlet extends HttpServlet {

    private final Gson gson = GsonProvider.gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> respuesta = new HashMap<>();
        String placa = req.getParameter("placa");
        if (placa != null) placa = placa.toUpperCase();

        try {
            EstacionamientoService servicio = (EstacionamientoService)
                    getServletContext().getAttribute(AppContextListener.ATRIBUTO_SERVICIO);

            // Busqueda O(log n) en el Arbol AVL en memoria
            Vehiculo vehiculo = servicio.buscarVehiculoEnMemoria(placa);

            if (vehiculo == null) {
                respuesta.put("encontrado", false);
                respuesta.put("mensaje", "Placa no registrada en el sistema.");
            } else {
                respuesta.put("encontrado", true);
                respuesta.put("vehiculo", vehiculo);
                Ticket activo = servicio.buscarTicketActivoDeVehiculo(vehiculo.getIdVehiculo());
                respuesta.put("ticketActivo", activo);
            }
        } catch (SQLException e) {
            respuesta.put("error", "Error de base de datos: " + e.getMessage());
        }

        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(gson.toJson(respuesta));
    }
}

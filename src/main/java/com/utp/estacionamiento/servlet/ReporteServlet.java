package com.utp.estacionamiento.servlet;

import com.google.gson.Gson;
import com.utp.estacionamiento.util.GsonProvider;
import com.utp.estacionamiento.dao.PagoDAO;
import com.utp.estacionamiento.dao.PagoDAOImpl;
import com.utp.estacionamiento.dao.ReporteIngresoDia;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReporteServlet extends HttpServlet {

    private final PagoDAO pagoDAO = new PagoDAOImpl();
    private final Gson gson = GsonProvider.gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> respuesta = new HashMap<>();
        try {
            LocalDate inicio = LocalDate.parse(req.getParameter("fechaInicio"));
            LocalDate fin = LocalDate.parse(req.getParameter("fechaFin"));
            List<ReporteIngresoDia> datos = pagoDAO.reporteIngresosPorRango(inicio, fin);
            respuesta.put("datos", datos);
        } catch (SQLException e) {
            respuesta.put("error", "Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            respuesta.put("error", "Fechas invalidas. Formato esperado: YYYY-MM-DD");
        }
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(gson.toJson(respuesta));
    }
}

package com.utp.estacionamiento.servlet;

import com.google.gson.Gson;
import com.utp.estacionamiento.util.GsonProvider;
import com.utp.estacionamiento.estructuras.MatrizEstacionamiento;
import com.utp.estacionamiento.modelo.EspacioEstacionamiento;
import com.utp.estacionamiento.servicio.EstacionamientoService;
import com.utp.estacionamiento.util.AppContextListener;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapaServlet extends HttpServlet {

    private final Gson gson = GsonProvider.gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        EstacionamientoService servicio = (EstacionamientoService)
                getServletContext().getAttribute(AppContextListener.ATRIBUTO_SERVICIO);

        MatrizEstacionamiento matriz = servicio.getMatriz();
        List<EspacioEstacionamiento> espacios = new ArrayList<>();
        for (int f = 1; f <= matriz.getFilas(); f++) {
            for (int c = 1; c <= matriz.getColumnas(); c++) {
                EspacioEstacionamiento e = matriz.obtener(f, c);
                if (e != null) espacios.add(e);
            }
        }

        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(gson.toJson(espacios));
    }
}

package com.utp.estacionamiento.servlet;

import com.google.gson.Gson;
import com.utp.estacionamiento.util.GsonProvider;
import com.utp.estacionamiento.dao.ReporteIngresoDia;
import com.utp.estacionamiento.estructuras.MatrizEstacionamiento;
import com.utp.estacionamiento.servicio.CompatibilidadEspacio;
import com.utp.estacionamiento.servicio.EstacionamientoService;
import com.utp.estacionamiento.util.AppContextListener;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Datos del panel de inicio. La ocupacion sale de la Matriz y los datos del
 * arbol salen del AVL, ambos en memoria; solo lo cobrado hoy se consulta en
 * MySQL con el mismo procedimiento del reporte.
 */
public class ResumenServlet extends HttpServlet {

    private final Gson gson = GsonProvider.gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> respuesta = new HashMap<>();
        EstacionamientoService servicio = (EstacionamientoService)
                getServletContext().getAttribute(AppContextListener.ATRIBUTO_SERVICIO);

        MatrizEstacionamiento matriz = servicio.getMatriz();
        int libresAuto = matriz.contarLibres(CompatibilidadEspacio.ESPACIO_AUTO);
        int totalAuto = matriz.contarTotal(CompatibilidadEspacio.ESPACIO_AUTO);
        int libresMoto = matriz.contarLibres(CompatibilidadEspacio.ESPACIO_MOTO);
        int totalMoto = matriz.contarTotal(CompatibilidadEspacio.ESPACIO_MOTO);
        respuesta.put("libresAuto", libresAuto);
        respuesta.put("totalAuto", totalAuto);
        respuesta.put("libresMoto", libresMoto);
        respuesta.put("totalMoto", totalMoto);
        respuesta.put("estacionados", (totalAuto - libresAuto) + (totalMoto - libresMoto));

        int placas = servicio.getTamanoArbol();
        respuesta.put("placasEnArbol", placas);
        respuesta.put("alturaArbol", servicio.getAlturaArbol());
        // Referencia teorica: un arbol perfectamente balanceado tiene altura ceil(log2(n + 1))
        respuesta.put("alturaMinima", (int) Math.ceil(Math.log(placas + 1) / Math.log(2)));

        try {
            LocalDate hoy = LocalDate.now();
            List<ReporteIngresoDia> datos = servicio.getPagoDAO().reporteIngresosPorRango(hoy, hoy);
            BigDecimal cobradoHoy = BigDecimal.ZERO;
            for (ReporteIngresoDia d : datos) {
                cobradoHoy = cobradoHoy.add(d.getIngresoTotal());
            }
            respuesta.put("cobradoHoy", cobradoHoy);
        } catch (SQLException e) {
            respuesta.put("error", "Error de base de datos: " + e.getMessage());
        }

        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(gson.toJson(respuesta));
    }
}

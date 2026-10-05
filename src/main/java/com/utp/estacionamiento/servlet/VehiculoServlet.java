package com.utp.estacionamiento.servlet;

import com.google.gson.Gson;
import com.utp.estacionamiento.util.GsonProvider;
import com.utp.estacionamiento.modelo.EspacioEstacionamiento;
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

/**
 * CRUD completo (Crear, Leer, Actualizar, Eliminar) con patron DAO.
 * Los cambios pasan por EstacionamientoService para que el Arbol AVL
 * quede sincronizado con la base de datos.
 */
public class VehiculoServlet extends HttpServlet {

    private static final int MYSQL_DUPLICADO = 1062;
    private static final int MYSQL_REFERENCIADO = 1451;

    private final Gson gson = GsonProvider.gson();

    private EstacionamientoService servicio() {
        return (EstacionamientoService) getServletContext().getAttribute(AppContextListener.ATRIBUTO_SERVICIO);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> respuesta = new HashMap<>();
        try {
            respuesta.put("vehiculos", servicio().getVehiculoDAO().listarTodos());
            // Vehiculos que estan dentro: idVehiculo -> numero de ticket y espacio
            Map<Integer, Map<String, Object>> activos = new HashMap<>();
            for (Ticket t : servicio().listarTicketsActivos()) {
                Map<String, Object> dato = new HashMap<>();
                dato.put("idTicket", t.getIdTicket());
                EspacioEstacionamiento e = servicio().getMatriz().buscarPorId(t.getIdEspacio());
                dato.put("numeroEspacio", e != null ? e.getNumeroEspacio() : null);
                activos.put(t.getIdVehiculo(), dato);
            }
            respuesta.put("activos", activos);
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
                    servicio().crearVehiculo(leerVehiculo(req));
                    respuesta.put("exito", true);
                    respuesta.put("mensaje", "Vehiculo agregado.");
                    break;
                }
                case "actualizar": {
                    Vehiculo v = leerVehiculo(req);
                    v.setIdVehiculo(Integer.parseInt(req.getParameter("idVehiculo")));
                    servicio().actualizarVehiculo(v);
                    respuesta.put("exito", true);
                    respuesta.put("mensaje", "Vehiculo actualizado.");
                    break;
                }
                case "eliminar": {
                    int id = Integer.parseInt(req.getParameter("idVehiculo"));
                    servicio().eliminarVehiculo(id);
                    respuesta.put("exito", true);
                    respuesta.put("mensaje", "Vehiculo eliminado.");
                    break;
                }
                default:
                    respuesta.put("exito", false);
                    respuesta.put("mensaje", "Accion no reconocida: " + accion);
            }
        } catch (SQLException e) {
            respuesta.put("exito", false);
            if (e.getErrorCode() == MYSQL_DUPLICADO) {
                respuesta.put("mensaje", "Ya existe un vehiculo con esa placa.");
            } else if (e.getErrorCode() == MYSQL_REFERENCIADO) {
                respuesta.put("mensaje", "No se puede eliminar: el vehiculo tiene tickets registrados.");
            } else {
                respuesta.put("mensaje", "Error de base de datos: " + e.getMessage());
            }
        }

        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(gson.toJson(respuesta));
    }

    private Vehiculo leerVehiculo(HttpServletRequest req) {
        Vehiculo v = new Vehiculo();
        v.setPlaca(req.getParameter("placa").trim().toUpperCase());
        v.setMarca(opcional(req.getParameter("marca")));
        v.setIdTipo(Integer.parseInt(req.getParameter("idTipo")));
        return v;
    }

    /** La marca es opcional: si llega vacia se guarda como NULL. */
    private static String opcional(String valor) {
        return (valor == null || valor.trim().isEmpty()) ? null : valor.trim();
    }
}

package com.utp.estacionamiento.util;

import com.utp.estacionamiento.servicio.EstacionamientoService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Se ejecuta una unica vez al arrancar la aplicacion: crea el
 * EstacionamientoService, carga el Arbol AVL y la Matriz desde
 * MySQL, y lo deja disponible para todos los Servlets a traves
 * del ServletContext.
 */
public class AppContextListener implements ServletContextListener {

    public static final String ATRIBUTO_SERVICIO = "estacionamientoService";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            EstacionamientoService servicio = new EstacionamientoService();
            servicio.inicializar();
            sce.getServletContext().setAttribute(ATRIBUTO_SERVICIO, servicio);
            System.out.println("EstacionamientoService inicializado correctamente.");
        } catch (Exception e) {
            throw new RuntimeException("Error al inicializar el servicio de estacionamiento. "
                    + "Verifica la conexion a MySQL en ConexionBD.java", e);
        }
    }
}

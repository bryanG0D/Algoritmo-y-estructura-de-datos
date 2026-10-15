package com.utp.estacionamiento.util;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controla el acceso a toda la aplicacion desde un solo lugar.
 *
 * Se ejecuta antes de cada pagina JSP y de cada Servlet (url-pattern "/*"
 * en web.xml). Si el usuario no inicio sesion:
 *   - a una pagina .jsp lo redirige a login.jsp
 *   - a un Servlet de datos (los que llama el JavaScript con fetch) le
 *     responde 401 con un JSON, y main.js lo manda al login.
 * El login, los estilos y los scripts quedan publicos.
 */
public class FiltroSesion implements Filter {

    @Override
    public void init(FilterConfig config) {
        // Vacio: Jetty 9.4 usa Servlet 3.1, donde init() es obligatorio
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String ruta = req.getRequestURI().substring(req.getContextPath().length());

        if (esRutaPublica(ruta) || haySesion(req)) {
            chain.doFilter(request, response);
            return;
        }

        if (ruta.equals("/") || ruta.endsWith(".jsp")) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
        } else {
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("exito", false);
            respuesta.put("mensaje", "Sesion expirada. Inicia sesion de nuevo.");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(GsonProvider.gson().toJson(respuesta));
        }
    }

    @Override
    public void destroy() {
        // Vacio, por la misma razon que init()
    }

    private boolean esRutaPublica(String ruta) {
        return ruta.equals("/login.jsp")
                || ruta.equals("/login")
                || ruta.startsWith("/css/")
                || ruta.startsWith("/js/");
    }

    private boolean haySesion(HttpServletRequest req) {
        HttpSession sesion = req.getSession(false);
        return sesion != null && sesion.getAttribute("usuario") != null;
    }
}

package com.utp.estacionamiento.servlet;

import com.google.gson.Gson;
import com.utp.estacionamiento.util.GsonProvider;
import com.utp.estacionamiento.dao.UsuarioDAO;
import com.utp.estacionamiento.dao.UsuarioDAOImpl;
import com.utp.estacionamiento.modelo.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class LoginServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
    private final Gson gson = GsonProvider.gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String login = req.getParameter("usuario");
        String clave = req.getParameter("clave");
        Map<String, Object> respuesta = new HashMap<>();

        try {
            Usuario usuario = usuarioDAO.buscarPorLogin(login);
            if (usuario != null && claveCorrecta(clave, usuario.getContrasenaHash())) {
                HttpSession sesion = req.getSession(true);
                sesion.setAttribute("usuario", usuario);
                respuesta.put("exito", true);
                respuesta.put("nombre", usuario.getNombre());
                respuesta.put("rol", usuario.getRol());
            } else {
                respuesta.put("exito", false);
                respuesta.put("mensaje", "Usuario o contrasena incorrectos.");
            }
        } catch (SQLException e) {
            respuesta.put("exito", false);
            respuesta.put("mensaje", "Error de base de datos: " + e.getMessage());
        }

        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(gson.toJson(respuesta));
    }

    // Compara la clave ingresada con el hash BCrypt guardado; un valor que no es BCrypt nunca coincide
    private boolean claveCorrecta(String clave, String hash) {
        if (clave == null || hash == null || !hash.startsWith("$2a$")) {
            return false;
        }
        try {
            return BCrypt.checkpw(clave, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

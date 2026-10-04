package com.utp.estacionamiento.conexion;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Punto unico de conexion a MySQL.
 *
 * Las credenciales NO estan escritas en el codigo: se leen del archivo
 * src/main/resources/db.properties, que cada integrante del grupo crea en
 * su propia computadora copiando db.properties.example. Ese archivo esta
 * en el .gitignore, asi que la contrasena de cada uno nunca se sube a
 * GitHub ni pisa la de los demas.
 *
 * Si el archivo no existe, se usan valores por defecto (root sin
 * contrasena en localhost), que funcionan en una instalacion basica de
 * MySQL o XAMPP.
 */
public class ConexionBD {

    private static final String ARCHIVO = "db.properties";

    private static final String URL_POR_DEFECTO =
            "jdbc:mysql://localhost:3306/estacionamiento_inteligente?useSSL=false&serverTimezone=America/Lima&allowPublicKeyRetrieval=true";
    private static final String USUARIO_POR_DEFECTO = "root";
    private static final String CONTRASENA_POR_DEFECTO = "";

    private static final String URL;
    private static final String USUARIO;
    private static final String CONTRASENA;

    static {
        Properties props = new Properties();
        try (InputStream in = ConexionBD.class.getClassLoader().getResourceAsStream(ARCHIVO)) {
            if (in != null) {
                props.load(in);
                System.out.println("ConexionBD: usando credenciales de " + ARCHIVO);
            } else {
                System.out.println("ConexionBD: no se encontro " + ARCHIVO
                        + ", usando valores por defecto (root sin contrasena). "
                        + "Copia db.properties.example como db.properties si necesitas otros datos.");
            }
        } catch (IOException e) {
            System.out.println("ConexionBD: error leyendo " + ARCHIVO + ", usando valores por defecto. " + e.getMessage());
        }

        URL = props.getProperty("db.url", URL_POR_DEFECTO);
        USUARIO = props.getProperty("db.usuario", USUARIO_POR_DEFECTO);
        CONTRASENA = props.getProperty("db.contrasena", CONTRASENA_POR_DEFECTO);

        String driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se encontro el driver " + driver + " en el classpath", e);
        }
    }

    private ConexionBD() {}

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }
}

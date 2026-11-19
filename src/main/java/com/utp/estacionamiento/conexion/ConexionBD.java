package com.utp.estacionamiento.conexion;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
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
 *
 * Usa un pool de conexiones (HikariCP): abrir una conexion cifrada a la base
 * en la nube tarda cerca de un segundo, asi que se mantienen algunas abiertas
 * y se reutilizan. Para los DAO no cambia nada: piden una conexion y al
 * cerrarla (try-with-resources) vuelve al pool en lugar de cerrarse.
 */
public class ConexionBD {

    private static final String ARCHIVO = "db.properties";

    private static final String URL_POR_DEFECTO =
            "jdbc:mysql://localhost:3306/estacionamiento_inteligente?useSSL=false&serverTimezone=America/Lima&allowPublicKeyRetrieval=true";
    private static final String USUARIO_POR_DEFECTO = "root";
    private static final String CONTRASENA_POR_DEFECTO = "";

    private static final HikariDataSource POOL;

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

        HikariConfig config = new HikariConfig();
        config.setDriverClassName(props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
        config.setJdbcUrl(props.getProperty("db.url", URL_POR_DEFECTO));
        config.setUsername(props.getProperty("db.usuario", USUARIO_POR_DEFECTO));
        config.setPassword(props.getProperty("db.contrasena", CONTRASENA_POR_DEFECTO));
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setPoolName("Estacionamiento");
        POOL = new HikariDataSource(config);
    }

    private ConexionBD() {}

    public static Connection obtenerConexion() throws SQLException {
        return POOL.getConnection();
    }

    /** Cierra las conexiones del pool al detener la aplicacion. */
    public static void cerrar() {
        POOL.close();
    }
}

package com.utp.estacionamiento.servicio;

import com.utp.estacionamiento.dao.EspacioEstacionamientoDAO;
import com.utp.estacionamiento.dao.EspacioEstacionamientoDAOImpl;
import com.utp.estacionamiento.dao.PagoDAO;
import com.utp.estacionamiento.dao.PagoDAOImpl;
import com.utp.estacionamiento.dao.ResultadoEntrada;
import com.utp.estacionamiento.dao.ResultadoSalida;
import com.utp.estacionamiento.dao.TicketDAO;
import com.utp.estacionamiento.dao.TicketDAOImpl;
import com.utp.estacionamiento.dao.TipoVehiculoDAO;
import com.utp.estacionamiento.dao.TipoVehiculoDAOImpl;
import com.utp.estacionamiento.dao.VehiculoDAO;
import com.utp.estacionamiento.dao.VehiculoDAOImpl;
import com.utp.estacionamiento.estructuras.ArbolAVL;
import com.utp.estacionamiento.estructuras.MatrizEstacionamiento;
import com.utp.estacionamiento.modelo.EspacioEstacionamiento;
import com.utp.estacionamiento.modelo.Ticket;
import com.utp.estacionamiento.modelo.TipoVehiculo;
import com.utp.estacionamiento.modelo.Vehiculo;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Nucleo "inteligente" del sistema. Integra dos estructuras en memoria con
 * la capa DAO:
 *   - Arbol AVL: busqueda de vehiculos por placa en O(log n).
 *   - Matriz 5x10: decide el espacio, eligiendo la fila con mas lugares libres.
 * MySQL persiste el resultado de cada operacion mediante procedimientos.
 *
 * No hay lista de espera: si no hay espacio compatible se informa y no se
 * crea ticket (indicacion del docente).
 */
public class EstacionamientoService {

    private final VehiculoDAO vehiculoDAO = new VehiculoDAOImpl();
    private final EspacioEstacionamientoDAO espacioDAO = new EspacioEstacionamientoDAOImpl();
    private final TicketDAO ticketDAO = new TicketDAOImpl();
    private final PagoDAO pagoDAO = new PagoDAOImpl();
    private final TipoVehiculoDAO tipoVehiculoDAO = new TipoVehiculoDAOImpl();

    private final ArbolAVL arbolPlacas = new ArbolAVL();
    private MatrizEstacionamiento matriz;

    /** Se ejecuta una sola vez al iniciar la aplicacion (ver AppContextListener). */
    public void inicializar() throws SQLException {
        for (Vehiculo v : vehiculoDAO.listarTodos()) {
            arbolPlacas.insertar(v);
        }

        List<EspacioEstacionamiento> espacios = espacioDAO.listarTodos();
        int maxFila = 0;
        int maxColumna = 0;
        for (EspacioEstacionamiento e : espacios) {
            maxFila = Math.max(maxFila, e.getFila());
            maxColumna = Math.max(maxColumna, e.getColumna());
        }
        matriz = new MatrizEstacionamiento(maxFila, maxColumna);
        matriz.cargarDesdeLista(espacios);
    }

    public static class ResultadoRegistro {
        public boolean asignado;
        public Integer idTicket;
        public String numeroEspacio;
        public String mensaje;
    }

    public synchronized ResultadoRegistro registrarEntrada(String placa, String marca, String modelo,
                                                           String color, int idTipo, int idUsuario)
            throws SQLException {
        ResultadoRegistro resultado = new ResultadoRegistro();

        // 1. Busqueda O(log n) en el Arbol AVL
        boolean yaConocido = arbolPlacas.buscarPorPlaca(placa) != null;
        String prefijo = yaConocido ? "Vehiculo reconocido (Arbol AVL). " : "Vehiculo nuevo registrado. ";

        // 2. Regla de compatibilidad: a que clase de espacio puede ir
        int tipoEspacio = CompatibilidadEspacio.tipoDeEspacioPara(idTipo);

        // 3. La Matriz elige el espacio (fila con mas lugares libres).
        //    Si la BD dice que ese espacio ya no estaba libre, se marca y se intenta una vez mas.
        for (int intento = 0; intento < 2; intento++) {
            EspacioEstacionamiento espacio = matriz.buscarEspacioEnFilaMasLibre(tipoEspacio);
            if (espacio == null) {
                break;
            }

            ResultadoEntrada r = ticketDAO.registrarEntrada(
                    placa, marca, modelo, color, idTipo, idUsuario, espacio.getIdEspacio());
            actualizarArbol(placa);

            if (r.fueAsignado()) {
                matriz.actualizarEstado(espacio.getIdEspacio(), "OCUPADO");
                resultado.asignado = true;
                resultado.idTicket = r.getIdTicket();
                resultado.numeroEspacio = espacio.getNumeroEspacio();
                resultado.mensaje = prefijo + "Espacio " + espacio.getNumeroEspacio()
                        + " asignado (fila " + espacio.getFila()
                        + ", la que tenia mas lugares libres).";
                return resultado;
            }
            matriz.actualizarEstado(espacio.getIdEspacio(), "OCUPADO");
        }

        resultado.asignado = false;
        resultado.mensaje = prefijo + "Estacionamiento lleno: no hay espacio disponible para este tipo de"
                + " vehiculo (espacios de " + CompatibilidadEspacio.nombreGrupo(tipoEspacio) + ").";
        return resultado;
    }

    /** Inserta el vehiculo en el AVL si es nuevo, o actualiza sus datos si ya existia. */
    private void actualizarArbol(String placa) throws SQLException {
        Vehiculo v = vehiculoDAO.buscarPorPlaca(placa);
        if (v != null) {
            arbolPlacas.insertar(v);
        }
    }

    public static class ResultadoCobro {
        public BigDecimal monto;
        public String numeroEspacioLiberado;
    }

    public synchronized ResultadoCobro registrarSalida(int idTicket, int idUsuarioCobro, String metodoPago)
            throws SQLException {
        ResultadoSalida r = ticketDAO.registrarSalida(idTicket, idUsuarioCobro, metodoPago);
        matriz.actualizarEstado(r.getIdEspacioLiberado(), "LIBRE");

        ResultadoCobro resultado = new ResultadoCobro();
        resultado.monto = r.getMonto();
        EspacioEstacionamiento espacio = matriz.buscarPorId(r.getIdEspacioLiberado());
        resultado.numeroEspacioLiberado = espacio != null ? espacio.getNumeroEspacio() : null;
        return resultado;
    }

    public Vehiculo buscarVehiculoEnMemoria(String placa) {
        return arbolPlacas.buscarPorPlaca(placa);
    }

    public Ticket buscarTicketActivoDeVehiculo(int idVehiculo) throws SQLException {
        for (Ticket t : ticketDAO.listarActivos()) {
            if (t.getIdVehiculo() == idVehiculo) {
                return t;
            }
        }
        return null;
    }

    public MatrizEstacionamiento getMatriz() { return matriz; }

    public List<TipoVehiculo> listarTiposVehiculo() throws SQLException {
        return tipoVehiculoDAO.listarTodos();
    }

    public VehiculoDAO getVehiculoDAO() { return vehiculoDAO; }
    public PagoDAO getPagoDAO() { return pagoDAO; }
}

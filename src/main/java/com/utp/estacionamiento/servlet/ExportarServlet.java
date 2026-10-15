package com.utp.estacionamiento.servlet;

import com.utp.estacionamiento.dao.PagoDAO;
import com.utp.estacionamiento.dao.PagoDAOImpl;
import com.utp.estacionamiento.dao.ReporteGrupo;
import com.utp.estacionamiento.dao.ReporteIngresoDia;
import com.utp.estacionamiento.modelo.EspacioEstacionamiento;
import com.utp.estacionamiento.modelo.Ticket;
import com.utp.estacionamiento.modelo.Vehiculo;
import com.utp.estacionamiento.servicio.EstacionamientoService;
import com.utp.estacionamiento.util.AppContextListener;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exporta a Excel (.xlsx) con Apache POI:
 *   /exportar?que=vehiculos&estado=todos|dentro|fuera&buscar=texto  (lo mismo que se ve en pantalla)
 *   /exportar?que=reporte&fechaInicio=AAAA-MM-DD&fechaFin=AAAA-MM-DD (4 hojas)
 */
public class ExportarServlet extends HttpServlet {

    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PagoDAO pagoDAO = new PagoDAOImpl();

    /** Estilos que se reutilizan en todas las hojas del libro. */
    private static class Estilos {
        final CellStyle titulo, encabezado, texto, soles, total, totalSoles, porcentaje;

        Estilos(XSSFWorkbook libro) {
            Font negrita = libro.createFont();
            negrita.setBold(true);
            Font blanca = libro.createFont();
            blanca.setBold(true);
            blanca.setColor(IndexedColors.WHITE.getIndex());
            Font grande = libro.createFont();
            grande.setBold(true);
            grande.setFontHeightInPoints((short) 14);
            short formatoSoles = libro.createDataFormat().getFormat("\"S/ \"#,##0.00");

            titulo = libro.createCellStyle();
            titulo.setFont(grande);
            encabezado = conBorde(libro);
            encabezado.setFont(blanca);
            encabezado.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            texto = conBorde(libro);
            soles = conBorde(libro);
            soles.setDataFormat(formatoSoles);
            total = conBorde(libro);
            total.setFont(negrita);
            total.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            total.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalSoles = conBorde(libro);
            totalSoles.cloneStyleFrom(total);
            totalSoles.setDataFormat(formatoSoles);
            porcentaje = conBorde(libro);
            porcentaje.setDataFormat(libro.createDataFormat().getFormat("0%"));
        }

        private static CellStyle conBorde(XSSFWorkbook libro) {
            CellStyle e = libro.createCellStyle();
            e.setBorderBottom(BorderStyle.THIN);
            e.setBorderTop(BorderStyle.THIN);
            e.setBorderLeft(BorderStyle.THIN);
            e.setBorderRight(BorderStyle.THIN);
            return e;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        EstacionamientoService servicio = (EstacionamientoService)
                getServletContext().getAttribute(AppContextListener.ATRIBUTO_SERVICIO);
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            Estilos estilos = new Estilos(libro);
            String archivo;
            if ("reporte".equals(req.getParameter("que"))) {
                LocalDate inicio = LocalDate.parse(req.getParameter("fechaInicio"));
                LocalDate fin = LocalDate.parse(req.getParameter("fechaFin"));
                hojasReporte(libro, estilos, inicio, fin);
                archivo = "reporte_" + inicio + "_al_" + fin + ".xlsx";
            } else {
                hojaVehiculos(libro, estilos, servicio, req.getParameter("estado"), req.getParameter("buscar"));
                archivo = "vehiculos_" + LocalDate.now() + ".xlsx";
            }
            resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + archivo + "\"");
            libro.write(resp.getOutputStream());
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error de base de datos: " + e.getMessage());
        } catch (RuntimeException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametros invalidos");
        }
    }

    private void hojaVehiculos(XSSFWorkbook libro, Estilos est, EstacionamientoService servicio,
                               String estado, String buscar) throws SQLException {
        Map<Integer, Ticket> activos = new HashMap<>();
        for (Ticket t : servicio.listarTicketsActivos()) {
            activos.put(t.getIdVehiculo(), t);
        }
        String texto = buscar == null ? "" : buscar.trim().toUpperCase();

        Sheet hoja = libro.createSheet("Vehiculos");
        titulo(hoja, est, "Vehiculos registrados - " + LocalDate.now().format(DIA));
        encabezados(hoja, est, 2, "Placa", "Marca", "Tipo", "Estado", "Ticket", "Espacio");
        int fila = 3;
        for (Vehiculo v : servicio.getVehiculoDAO().listarTodos()) {
            Ticket t = activos.get(v.getIdVehiculo());
            if ("dentro".equals(estado) && t == null) continue;
            if ("fuera".equals(estado) && t != null) continue;
            String marca = v.getMarca() == null ? "" : v.getMarca();
            if (!texto.isEmpty() && !v.getPlaca().contains(texto) && !marca.toUpperCase().contains(texto)) continue;

            Row r = hoja.createRow(fila++);
            celda(r, 0, v.getPlaca(), est.texto);
            celda(r, 1, marca, est.texto);
            celda(r, 2, v.getNombreTipo(), est.texto);
            celda(r, 3, t == null ? "Fuera" : "Dentro", est.texto);
            celda(r, 4, t == null ? "" : String.valueOf(t.getIdTicket()), est.texto);
            EspacioEstacionamiento e = t == null ? null : servicio.getMatriz().buscarPorId(t.getIdEspacio());
            celda(r, 5, e == null ? "" : e.getNumeroEspacio(), est.texto);
        }
        Row r = hoja.createRow(fila + 1);
        celda(r, 0, "Total: " + (fila - 3) + " vehiculos", est.total);
        ajustarColumnas(hoja, 6);
    }

    private void hojasReporte(XSSFWorkbook libro, Estilos est, LocalDate inicio, LocalDate fin) throws SQLException {
        List<ReporteIngresoDia> dias = pagoDAO.reporteIngresosPorRango(inicio, fin);
        String periodo = inicio.format(DIA) + " al " + fin.format(DIA);

        BigDecimal total = BigDecimal.ZERO;
        int tickets = 0;
        ReporteIngresoDia mejor = null;
        for (ReporteIngresoDia d : dias) {
            total = total.add(d.getIngresoTotal());
            tickets += d.getCantidadTickets();
            if (mejor == null || d.getIngresoTotal().compareTo(mejor.getIngresoTotal()) > 0) mejor = d;
        }

        Sheet resumen = libro.createSheet("Resumen");
        titulo(resumen, est, "Reporte de ingresos - " + periodo);
        encabezados(resumen, est, 2, "Indicador", "Valor");
        filaDato(resumen, est, 3, "Total recaudado", total);
        Row r = resumen.createRow(4);
        celda(r, 0, "Tickets cobrados", est.texto);
        celdaNumero(r, 1, tickets, est.texto);
        filaDato(resumen, est, 5, "Ticket promedio",
                tickets == 0 ? BigDecimal.ZERO : total.divide(BigDecimal.valueOf(tickets), 2, RoundingMode.HALF_UP));
        r = resumen.createRow(6);
        celda(r, 0, "Mejor dia", est.texto);
        celda(r, 1, mejor == null ? "-" : mejor.getFecha().format(DIA) + " (S/ " + mejor.getIngresoTotal() + ")", est.texto);
        ajustarColumnas(resumen, 2);

        Sheet porDia = libro.createSheet("Por dia");
        titulo(porDia, est, "Ingresos por dia - " + periodo);
        encabezados(porDia, est, 2, "Fecha", "Tickets", "Ingreso");
        int fila = 3;
        for (ReporteIngresoDia d : dias) {
            r = porDia.createRow(fila++);
            celda(r, 0, d.getFecha().format(DIA), est.texto);
            celdaNumero(r, 1, d.getCantidadTickets(), est.texto);
            celdaSoles(r, 2, d.getIngresoTotal(), est.soles);
        }
        r = porDia.createRow(fila);
        celda(r, 0, "Total", est.total);
        celdaNumero(r, 1, tickets, est.total);
        celdaSoles(r, 2, total, est.totalSoles);
        ajustarColumnas(porDia, 3);

        hojaGrupo(libro, est, "Por tipo de vehiculo", periodo, "Tipo", "Tickets", pagoDAO.reportePorTipo(inicio, fin), total);
        hojaGrupo(libro, est, "Por metodo de pago", periodo, "Metodo", "Pagos", pagoDAO.reportePorMetodoPago(inicio, fin), total);
    }

    private void hojaGrupo(XSSFWorkbook libro, Estilos est, String nombre, String periodo, String columna,
                           String cantidad, List<ReporteGrupo> grupos, BigDecimal total) {
        Sheet hoja = libro.createSheet(nombre);
        titulo(hoja, est, nombre + " - " + periodo);
        encabezados(hoja, est, 2, columna, cantidad, "Ingreso", "%");
        int fila = 3;
        for (ReporteGrupo g : grupos) {
            Row r = hoja.createRow(fila++);
            celda(r, 0, nombreLegible(g.getNombre()), est.texto);
            celdaNumero(r, 1, g.getCantidad(), est.texto);
            celdaSoles(r, 2, g.getIngreso(), est.soles);
            Cell pct = r.createCell(3);
            pct.setCellValue(total.signum() == 0 ? 0 : g.getIngreso().doubleValue() / total.doubleValue());
            pct.setCellStyle(est.porcentaje);
        }
        Row r = hoja.createRow(fila);
        celda(r, 0, "Total", est.total);
        celdaNumero(r, 1, grupos.stream().mapToInt(ReporteGrupo::getCantidad).sum(), est.total);
        celdaSoles(r, 2, total, est.totalSoles);
        ajustarColumnas(hoja, 4);
    }

    /** EFECTIVO -> Efectivo (los tipos de vehiculo ya vienen legibles). */
    private static String nombreLegible(String nombre) {
        if (nombre == null || nombre.isEmpty() || !nombre.equals(nombre.toUpperCase())) return nombre;
        return nombre.charAt(0) + nombre.substring(1).toLowerCase();
    }

    private static void titulo(Sheet hoja, Estilos est, String texto) {
        celda(hoja.createRow(0), 0, texto, est.titulo);
    }

    private static void encabezados(Sheet hoja, Estilos est, int fila, String... nombres) {
        Row r = hoja.createRow(fila);
        for (int i = 0; i < nombres.length; i++) celda(r, i, nombres[i], est.encabezado);
    }

    private static void filaDato(Sheet hoja, Estilos est, int fila, String etiqueta, BigDecimal monto) {
        Row r = hoja.createRow(fila);
        celda(r, 0, etiqueta, est.texto);
        celdaSoles(r, 1, monto, est.soles);
    }

    private static void celda(Row r, int col, String valor, CellStyle estilo) {
        Cell c = r.createCell(col);
        c.setCellValue(valor == null ? "" : valor);
        c.setCellStyle(estilo);
    }

    private static void celdaNumero(Row r, int col, int valor, CellStyle estilo) {
        Cell c = r.createCell(col);
        c.setCellValue(valor);
        c.setCellStyle(estilo);
    }

    private static void celdaSoles(Row r, int col, BigDecimal valor, CellStyle estilo) {
        Cell c = r.createCell(col);
        c.setCellValue(valor == null ? 0 : valor.doubleValue());
        c.setCellStyle(estilo);
    }

    /** El titulo (fila 0) ocupa todas las columnas de la tabla, asi no ensancha la primera. */
    private static void ajustarColumnas(Sheet hoja, int columnas) {
        if (columnas > 1) hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, columnas - 1));
        for (int i = 0; i < columnas; i++) {
            hoja.autoSizeColumn(i);
            hoja.setColumnWidth(i, Math.max(hoja.getColumnWidth(i) + 512, 12 * 256));
        }
    }
}

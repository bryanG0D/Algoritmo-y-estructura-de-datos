package com.utp.estacionamiento.estructuras;

import com.utp.estacionamiento.modelo.EspacioEstacionamiento;

import java.util.List;

/**
 * Representa el estacionamiento como una matriz bidimensional en
 * memoria (arreglo 2D). El sistema "comprende" la distribucion
 * fisica del lugar y elige el espacio de la fila con mas lugares libres,
 * guiando a los vehiculos hacia la zona mas vacia.
 */
public class MatrizEstacionamiento {

    private final EspacioEstacionamiento[][] matriz;
    private final int filas;
    private final int columnas;

    public MatrizEstacionamiento(int filas, int columnas) {
        this.filas = filas;
        this.columnas = columnas;
        this.matriz = new EspacioEstacionamiento[filas][columnas];
    }

    public void cargarDesdeLista(List<EspacioEstacionamiento> espacios) {
        for (EspacioEstacionamiento e : espacios) {
            int f = e.getFila() - 1;
            int c = e.getColumna() - 1;
            if (f >= 0 && f < filas && c >= 0 && c < columnas) {
                matriz[f][c] = e;
            }
        }
    }

    public EspacioEstacionamiento obtener(int fila, int columna) {
        return matriz[fila - 1][columna - 1];
    }

    /** Cuenta los espacios libres de un tipo en una fila (fila con base 1). */
    public int contarLibresEnFila(int fila, int idTipoPermitido) {
        int libres = 0;
        for (int c = 0; c < columnas; c++) {
            EspacioEstacionamiento e = matriz[fila - 1][c];
            if (e != null && e.estaLibre() && e.getIdTipoPermitido() == idTipoPermitido) {
                libres++;
            }
        }
        return libres;
    }

    /** Cuenta los espacios libres de un tipo en toda la matriz. */
    public int contarLibres(int idTipoPermitido) {
        int libres = 0;
        for (int f = 1; f <= filas; f++) {
            libres += contarLibresEnFila(f, idTipoPermitido);
        }
        return libres;
    }

    /** Cuenta todos los espacios de un tipo (libres u ocupados). */
    public int contarTotal(int idTipoPermitido) {
        int total = 0;
        for (EspacioEstacionamiento[] fila : matriz) {
            for (EspacioEstacionamiento e : fila) {
                if (e != null && e.getIdTipoPermitido() == idTipoPermitido) {
                    total++;
                }
            }
        }
        return total;
    }

    /**
     * Asignacion inteligente: busca la fila con MAS espacios libres del tipo
     * pedido (en empate, la de menor numero) y devuelve el primer espacio
     * libre de esa fila. Asi los vehiculos se reparten hacia la zona mas
     * vacia del estacionamiento. Devuelve null si no hay ningun espacio.
     * Complejidad O(filas x columnas).
     */
    public EspacioEstacionamiento buscarEspacioEnFilaMasLibre(int idTipoPermitido) {
        int mejorFila = -1;
        int maxLibres = 0;
        for (int f = 1; f <= filas; f++) {
            int libres = contarLibresEnFila(f, idTipoPermitido);
            if (libres > maxLibres) {
                maxLibres = libres;
                mejorFila = f;
            }
        }
        if (mejorFila == -1) {
            return null;
        }
        for (int c = 0; c < columnas; c++) {
            EspacioEstacionamiento e = matriz[mejorFila - 1][c];
            if (e != null && e.estaLibre() && e.getIdTipoPermitido() == idTipoPermitido) {
                return e;
            }
        }
        return null;
    }

    public EspacioEstacionamiento buscarPorId(int idEspacio) {
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                EspacioEstacionamiento e = matriz[f][c];
                if (e != null && e.getIdEspacio() == idEspacio) {
                    return e;
                }
            }
        }
        return null;
    }

    public void actualizarEstado(int idEspacio, String nuevoEstado) {
        EspacioEstacionamiento e = buscarPorId(idEspacio);
        if (e != null) {
            e.setEstado(nuevoEstado);
        }
    }

    public EspacioEstacionamiento[][] getMatriz() { return matriz; }
    public int getFilas() { return filas; }
    public int getColumnas() { return columnas; }
}

package com.utp.estacionamiento.estructuras;

import com.utp.estacionamiento.modelo.Vehiculo;

import java.util.List;

/**
 * Resultado de una busqueda en el Arbol AVL: el vehiculo encontrado (o null),
 * cuantas comparaciones se hicieron y las placas de los nodos visitados desde
 * la raiz. Sirve para mostrar que la busqueda es O(log n): las comparaciones
 * nunca superan la altura del arbol.
 */
public class ResultadoBusquedaAVL {

    private final Vehiculo vehiculo;
    private final int comparaciones;
    private final List<String> recorrido;

    public ResultadoBusquedaAVL(Vehiculo vehiculo, int comparaciones, List<String> recorrido) {
        this.vehiculo = vehiculo;
        this.comparaciones = comparaciones;
        this.recorrido = recorrido;
    }

    public Vehiculo getVehiculo() { return vehiculo; }
    public int getComparaciones() { return comparaciones; }
    public List<String> getRecorrido() { return recorrido; }
}

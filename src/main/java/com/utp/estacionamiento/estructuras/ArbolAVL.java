package com.utp.estacionamiento.estructuras;

import com.utp.estacionamiento.modelo.Vehiculo;

import java.util.ArrayList;
import java.util.List;

/**
 * Arbol AVL indexado por placa. Permite busqueda, insercion y
 * eliminacion en O(log n): localiza un vehiculo por placa en
 * milisegundos sin recorrer toda la lista, descartando la mitad
 * del arbol en cada comparacion.
 */
public class ArbolAVL {

    private NodoAVL raiz;

    private int altura(NodoAVL nodo) {
        return nodo == null ? 0 : nodo.altura;
    }

    private int factorBalance(NodoAVL nodo) {
        return nodo == null ? 0 : altura(nodo.izquierdo) - altura(nodo.derecho);
    }

    private void actualizarAltura(NodoAVL nodo) {
        nodo.altura = 1 + Math.max(altura(nodo.izquierdo), altura(nodo.derecho));
    }

    private NodoAVL rotarDerecha(NodoAVL y) {
        NodoAVL x = y.izquierdo;
        NodoAVL t2 = x.derecho;
        x.derecho = y;
        y.izquierdo = t2;
        actualizarAltura(y);
        actualizarAltura(x);
        return x;
    }

    private NodoAVL rotarIzquierda(NodoAVL x) {
        NodoAVL y = x.derecho;
        NodoAVL t2 = y.izquierdo;
        y.izquierdo = x;
        x.derecho = t2;
        actualizarAltura(x);
        actualizarAltura(y);
        return y;
    }

    private NodoAVL balancear(NodoAVL nodo) {
        actualizarAltura(nodo);
        int balance = factorBalance(nodo);

        if (balance > 1) {
            if (factorBalance(nodo.izquierdo) < 0) {
                nodo.izquierdo = rotarIzquierda(nodo.izquierdo);
            }
            return rotarDerecha(nodo);
        }
        if (balance < -1) {
            if (factorBalance(nodo.derecho) > 0) {
                nodo.derecho = rotarDerecha(nodo.derecho);
            }
            return rotarIzquierda(nodo);
        }
        return nodo;
    }

    public void insertar(Vehiculo vehiculo) {
        raiz = insertar(raiz, vehiculo);
    }

    private NodoAVL insertar(NodoAVL nodo, Vehiculo vehiculo) {
        if (nodo == null) {
            return new NodoAVL(vehiculo);
        }
        int cmp = vehiculo.getPlaca().compareTo(nodo.vehiculo.getPlaca());
        if (cmp < 0) {
            nodo.izquierdo = insertar(nodo.izquierdo, vehiculo);
        } else if (cmp > 0) {
            nodo.derecho = insertar(nodo.derecho, vehiculo);
        } else {
            nodo.vehiculo = vehiculo; // la placa ya existia: se actualizan sus datos
            return nodo;
        }
        return balancear(nodo);
    }

    public Vehiculo buscarPorPlaca(String placa) {
        return buscarConRecorrido(placa).getVehiculo();
    }

    /**
     * Busca una placa bajando desde la raiz: en cada nodo compara y descarta
     * la mitad del arbol (izquierda si la placa es menor, derecha si es mayor).
     * Ademas registra cuantas comparaciones hizo y que nodos visito.
     */
    public ResultadoBusquedaAVL buscarConRecorrido(String placa) {
        List<String> recorrido = new ArrayList<>();
        NodoAVL actual = raiz;
        while (actual != null) {
            recorrido.add(actual.vehiculo.getPlaca());
            int cmp = placa.compareTo(actual.vehiculo.getPlaca());
            if (cmp == 0) {
                return new ResultadoBusquedaAVL(actual.vehiculo, recorrido.size(), recorrido);
            }
            actual = cmp < 0 ? actual.izquierdo : actual.derecho;
        }
        return new ResultadoBusquedaAVL(null, recorrido.size(), recorrido);
    }

    public void eliminar(String placa) {
        raiz = eliminar(raiz, placa);
    }

    private NodoAVL eliminar(NodoAVL nodo, String placa) {
        if (nodo == null) return null;
        int cmp = placa.compareTo(nodo.vehiculo.getPlaca());
        if (cmp < 0) {
            nodo.izquierdo = eliminar(nodo.izquierdo, placa);
        } else if (cmp > 0) {
            nodo.derecho = eliminar(nodo.derecho, placa);
        } else {
            if (nodo.izquierdo == null) return nodo.derecho;
            if (nodo.derecho == null) return nodo.izquierdo;
            NodoAVL sucesor = nodo.derecho;
            while (sucesor.izquierdo != null) sucesor = sucesor.izquierdo;
            nodo.vehiculo = sucesor.vehiculo;
            nodo.derecho = eliminar(nodo.derecho, sucesor.vehiculo.getPlaca());
        }
        return balancear(nodo);
    }

    public List<Vehiculo> listarEnOrden() {
        List<Vehiculo> resultado = new ArrayList<>();
        listarEnOrden(raiz, resultado);
        return resultado;
    }

    private void listarEnOrden(NodoAVL nodo, List<Vehiculo> resultado) {
        if (nodo == null) return;
        listarEnOrden(nodo.izquierdo, resultado);
        resultado.add(nodo.vehiculo);
        listarEnOrden(nodo.derecho, resultado);
    }

    /** Cantidad de vehiculos en el arbol (cuenta los nodos recursivamente). */
    public int tamano() {
        return contarNodos(raiz);
    }

    private int contarNodos(NodoAVL nodo) {
        if (nodo == null) return 0;
        return 1 + contarNodos(nodo.izquierdo) + contarNodos(nodo.derecho);
    }

    /** Altura del arbol: maximo de comparaciones que puede necesitar una busqueda. */
    public int altura() {
        return altura(raiz);
    }
}

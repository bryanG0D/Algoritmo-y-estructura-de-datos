package com.utp.estacionamiento.estructuras;

import com.utp.estacionamiento.modelo.Vehiculo;

public class NodoAVL {
    Vehiculo vehiculo;
    NodoAVL izquierdo;
    NodoAVL derecho;
    int altura;

    public NodoAVL(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
        this.altura = 1;
    }
}

package com.utp.estacionamiento.modelo;

public class EspacioEstacionamiento {
    private int idEspacio;
    private int idZona;
    private String numeroEspacio;
    private int fila;
    private int columna;
    private String estado; // LIBRE, OCUPADO, MANTENIMIENTO
    private int idTipoPermitido;

    public EspacioEstacionamiento() {}

    public EspacioEstacionamiento(int idEspacio, int idZona, String numeroEspacio,
                                   int fila, int columna, String estado, int idTipoPermitido) {
        this.idEspacio = idEspacio;
        this.idZona = idZona;
        this.numeroEspacio = numeroEspacio;
        this.fila = fila;
        this.columna = columna;
        this.estado = estado;
        this.idTipoPermitido = idTipoPermitido;
    }

    public int getIdEspacio() { return idEspacio; }
    public void setIdEspacio(int idEspacio) { this.idEspacio = idEspacio; }
    public int getIdZona() { return idZona; }
    public void setIdZona(int idZona) { this.idZona = idZona; }
    public String getNumeroEspacio() { return numeroEspacio; }
    public void setNumeroEspacio(String numeroEspacio) { this.numeroEspacio = numeroEspacio; }
    public int getFila() { return fila; }
    public void setFila(int fila) { this.fila = fila; }
    public int getColumna() { return columna; }
    public void setColumna(int columna) { this.columna = columna; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public int getIdTipoPermitido() { return idTipoPermitido; }
    public void setIdTipoPermitido(int idTipoPermitido) { this.idTipoPermitido = idTipoPermitido; }

    public boolean estaLibre() {
        return "LIBRE".equalsIgnoreCase(estado);
    }
}

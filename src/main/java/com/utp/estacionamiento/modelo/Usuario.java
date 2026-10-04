package com.utp.estacionamiento.modelo;

import java.io.Serializable;

public class Usuario implements Serializable {
    private int idUsuario;
    private String nombre;
    private String usuarioLogin;
    private String contrasenaHash;
    private String rol;

    public Usuario() {}

    public Usuario(int idUsuario, String nombre, String usuarioLogin, String contrasenaHash, String rol) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.usuarioLogin = usuarioLogin;
        this.contrasenaHash = contrasenaHash;
        this.rol = rol;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUsuarioLogin() { return usuarioLogin; }
    public void setUsuarioLogin(String usuarioLogin) { this.usuarioLogin = usuarioLogin; }
    public String getContrasenaHash() { return contrasenaHash; }
    public void setContrasenaHash(String contrasenaHash) { this.contrasenaHash = contrasenaHash; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}

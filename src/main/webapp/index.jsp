<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.utp.estacionamiento.modelo.Usuario" %>
<%
    Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Panel - Estacionamiento Inteligente</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<header class="barra-superior">
    <h1>Estacionamiento Inteligente</h1>
    <span>Hola, <%= usuarioSesion.getNombre() %> (<%= usuarioSesion.getRol() %>)</span>
    <a href="logout" class="boton-salir">Salir</a>
</header>
<nav class="menu-principal">
    <a href="entrada.jsp">Registrar entrada</a>
    <a href="salida.jsp">Registrar salida</a>
    <a href="busqueda.jsp">Buscar por placa</a>
    <a href="mapa.jsp">Mapa del estacionamiento</a>
    <a href="vehiculos.jsp">Vehiculos</a>
    <a href="reporte.jsp">Reporte de ingresos</a>
</nav>
</body>
</html>

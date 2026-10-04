<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    if (session.getAttribute("usuario") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Registrar entrada</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<header class="barra-superior">
    <h1>Registrar entrada</h1>
    <a href="index.jsp" class="boton-salir">Volver</a>
</header>
<main>
    <form id="formEntrada">
        <label>Placa</label>
        <input type="text" name="placa" id="placa" required>
        <label>Marca</label>
        <input type="text" name="marca" id="marca" required>
        <label>Modelo</label>
        <input type="text" name="modelo" id="modelo" required>
        <label>Color</label>
        <input type="text" name="color" id="color" required>
        <label>Tipo de vehiculo</label>
        <select name="idTipo" id="idTipo" required></select>
        <button type="submit">Registrar</button>
    </form>
    <div id="resultado"></div>
</main>
<script src="js/main.js"></script>
<script>
cargarTiposVehiculo('idTipo');

document.getElementById('formEntrada').addEventListener('submit', function (e) {
    e.preventDefault();
    const datos = new URLSearchParams(new FormData(e.target));

    fetch('entrada', { method: 'POST', body: datos })
        .then(r => r.json())
        .then(r => {
            const div = document.getElementById('resultado');
            if (r.error) {
                div.innerHTML = '<p class="error">' + r.error + '</p>';
            } else if (r.asignado) {
                div.innerHTML = '<p class="exito">Ticket #' + r.idTicket +
                    ' creado. Espacio asignado: ' + r.numeroEspacio + '</p>';
            } else {
                div.innerHTML = '<p class="aviso">' + r.mensaje + '</p>';
            }
            e.target.reset();
            cargarTiposVehiculo('idTipo');
        });
});
</script>
</body>
</html>

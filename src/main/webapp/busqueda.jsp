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
    <title>Buscar por placa</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<header class="barra-superior">
    <h1>Buscar vehiculo por placa</h1>
    <a href="index.jsp" class="boton-salir">Volver</a>
</header>
<main>
    <form id="formBusqueda">
        <label>Placa</label>
        <input type="text" name="placa" id="placa" required>
        <button type="submit">Buscar</button>
    </form>
    <div id="resultado"></div>
</main>
<script src="js/main.js"></script>
<script>
document.getElementById('formBusqueda').addEventListener('submit', function (e) {
    e.preventDefault();
    const placa = document.getElementById('placa').value;

    fetch('buscar?placa=' + encodeURIComponent(placa))
        .then(r => r.json())
        .then(r => {
            const div = document.getElementById('resultado');
            if (!r.encontrado) {
                div.innerHTML = '<p class="aviso">' + r.mensaje + '</p>';
                return;
            }
            const v = r.vehiculo;
            let html = '<p><strong>' + v.placa + '</strong> - ' + v.marca + ' ' + v.modelo + ' (' + v.color + ')</p>';
            if (r.ticketActivo) {
                html += '<p class="exito">Ticket activo #' + r.ticketActivo.idTicket +
                    ' desde ' + r.ticketActivo.fechaHoraEntrada + '</p>';
            } else {
                html += '<p class="aviso">Sin ticket activo actualmente.</p>';
            }
            div.innerHTML = html;
        });
});
</script>
</body>
</html>

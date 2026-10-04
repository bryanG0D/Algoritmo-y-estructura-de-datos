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
    <title>Reporte de ingresos</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<header class="barra-superior">
    <h1>Reporte de ingresos</h1>
    <a href="index.jsp" class="boton-salir">Volver</a>
</header>
<main>
    <form id="formReporte">
        <label>Desde</label>
        <input type="date" name="fechaInicio" id="fechaInicio" required>
        <label>Hasta</label>
        <input type="date" name="fechaFin" id="fechaFin" required>
        <button type="submit">Generar</button>
    </form>
    <table id="tablaReporte">
        <thead><tr><th>Fecha</th><th>Tickets</th><th>Ingreso total (S/)</th></tr></thead>
        <tbody></tbody>
    </table>
</main>
<script src="js/main.js"></script>
<script>
document.getElementById('formReporte').addEventListener('submit', function (e) {
    e.preventDefault();
    const inicio = document.getElementById('fechaInicio').value;
    const fin = document.getElementById('fechaFin').value;

    fetch('reporte?fechaInicio=' + inicio + '&fechaFin=' + fin)
        .then(r => r.json())
        .then(r => {
            const tbody = document.querySelector('#tablaReporte tbody');
            tbody.innerHTML = '';
            if (r.error) {
                tbody.innerHTML = '<tr><td colspan="3">' + r.error + '</td></tr>';
                return;
            }
            (r.datos || []).forEach(d => {
                const fila = document.createElement('tr');
                fila.innerHTML = '<td>' + d.fecha + '</td><td>' + d.cantidadTickets +
                    '</td><td>' + d.ingresoTotal + '</td>';
                tbody.appendChild(fila);
            });
        });
});
</script>
</body>
</html>

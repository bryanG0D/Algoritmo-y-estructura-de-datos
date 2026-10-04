<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
            const traza = recorridoAVL(r);
            if (!r.encontrado) {
                div.innerHTML = '<p class="aviso">' + r.mensaje + '</p>' + traza;
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
            div.innerHTML = html + traza;
        });
});

// Muestra como bajo la busqueda por el Arbol AVL: nodos visitados y comparaciones.
function recorridoAVL(r) {
    if (r.comparaciones === undefined) return '';
    const ultimo = r.recorrido.length - 1;
    const pasos = r.recorrido.map((p, i) =>
        '<span class="nodo-avl' + (r.encontrado && i === ultimo ? ' encontrado' : '') + '">' + p + '</span>').join(' &rarr; ');
    return '<div class="panel-avl">' +
        '<p><strong>Arbol AVL:</strong> ' + r.comparaciones + ' comparaciones entre ' +
        r.totalPlacas + ' placas (altura del arbol: ' + r.alturaArbol + ').</p>' +
        '<p class="recorrido-avl">' + (pasos || 'Arbol vacio') + '</p>' +
        '<p class="nota">Una lista recorrida de inicio a fin podria necesitar hasta ' +
        r.totalPlacas + ' comparaciones.</p></div>';
}
</script>
</body>
</html>

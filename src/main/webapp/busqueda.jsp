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
    const placa = document.getElementById('placa').value.trim();

    fetch('buscar?placa=' + encodeURIComponent(placa))
        .then(r => r.json())
        .then(r => {
            document.getElementById('resultado').innerHTML = fichaVehiculo(r) + detalleTecnico(r);
        });
});

// Detalle tecnico plegado: como se encontro la placa en el Arbol AVL (para la exposicion).
function detalleTecnico(r) {
    if (r.comparaciones === undefined) return '';
    const ultimo = r.recorrido.length - 1;
    const pasos = r.recorrido.map((p, i) =>
        '<span class="nodo-avl' + (r.encontrado && i === ultimo ? ' encontrado' : '') + '">' + escaparHtml(p) + '</span>').join(' &rarr; ');
    return '<details class="detalle-tecnico"><summary>Detalle tecnico de la busqueda</summary>' +
        '<p>Busqueda en Arbol AVL: ' + r.comparaciones + ' comparaciones entre ' +
        r.totalPlacas + ' placas (altura del arbol: ' + r.alturaArbol + ').</p>' +
        '<p class="recorrido-avl">' + (pasos || 'Arbol vacio') + '</p>' +
        '<p class="nota">Recorriendo una lista de inicio a fin podrian ser hasta ' +
        r.totalPlacas + ' comparaciones.</p></details>';
}
</script>
</body>
</html>

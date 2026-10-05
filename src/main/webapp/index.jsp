<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Inicio - Estacionamiento</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<% String paginaActual = "inicio"; %>
<%@ include file="/WEB-INF/menu.jspf" %>

<main class="panel-inicio">
    <section class="tarjetas">
        <div class="tarjeta"><span class="dato" id="libresAuto">-</span><span class="etiqueta">Espacios de auto libres</span></div>
        <div class="tarjeta"><span class="dato" id="libresMoto">-</span><span class="etiqueta">Espacios de moto libres</span></div>
        <div class="tarjeta"><span class="dato" id="estacionados">-</span><span class="etiqueta">Vehiculos estacionados</span></div>
        <div class="tarjeta"><span class="dato" id="cobradoHoy">-</span><span class="etiqueta">Cobrado hoy</span></div>
    </section>

    <section class="columnas-inicio">
        <div class="caja">
            <h2>Ocupacion del estacionamiento</h2>
            <div id="miniMapa"></div>
            <p class="nota">
                <span class="celda libre"></span> Libre
                <span class="celda ocupado"></span> Ocupado
                &nbsp; Borde azul: motos y bicicletas
            </p>
            <a href="mapa.jsp">Ver mapa completo &rarr;</a>
        </div>
        <div class="caja">
            <h2>Buscar vehiculo por placa</h2>
            <p class="nota">Para clientes que perdieron su ticket o para ubicar un vehiculo.</p>
            <form id="formBusquedaRapida" class="busqueda-rapida">
                <input type="text" id="placaRapida" placeholder="Ej. ABC-123" required>
                <button type="submit">Buscar</button>
            </form>
            <div id="resultadoRapido"></div>
            <p class="nota"><span id="vehiculosRegistrados">-</span> vehiculos registrados en el sistema.</p>
        </div>
    </section>
</main>

<script src="js/main.js"></script>
<script>
function cargarResumen() {
    fetch('resumen')
        .then(r => r.json())
        .then(r => {
            document.getElementById('libresAuto').textContent = r.libresAuto + ' / ' + r.totalAuto;
            document.getElementById('libresMoto').textContent = r.libresMoto + ' / ' + r.totalMoto;
            document.getElementById('estacionados').textContent = r.estacionados;
            document.getElementById('cobradoHoy').textContent =
                r.cobradoHoy !== undefined ? 'S/ ' + Number(r.cobradoHoy).toFixed(2) : 'sin datos';
            document.getElementById('vehiculosRegistrados').textContent = r.placasEnArbol;
        });
}

// Busqueda rapida: usa el mismo /buscar (Arbol AVL) y muestra la ficha del vehiculo.
document.getElementById('formBusquedaRapida').addEventListener('submit', function (e) {
    e.preventDefault();
    const placa = document.getElementById('placaRapida').value.trim();
    fetch('buscar?placa=' + encodeURIComponent(placa))
        .then(r => r.json())
        .then(r => { document.getElementById('resultadoRapido').innerHTML = fichaVehiculo(r) + detalleTecnico(r); });
});

function cargarMiniMapa() {
    fetch('mapa')
        .then(r => r.json())
        .then(espacios => {
            const tabla = document.createElement('table');
            const filas = Math.max(...espacios.map(e => e.fila));
            const columnas = Math.max(...espacios.map(e => e.columna));
            for (let f = 1; f <= filas; f++) {
                const tr = document.createElement('tr');
                for (let c = 1; c <= columnas; c++) {
                    const e = espacios.find(x => x.fila === f && x.columna === c);
                    const td = document.createElement('td');
                    if (e) {
                        td.className = 'celda ' + (e.estado === 'LIBRE' ? 'libre' : 'ocupado') +
                            (e.idTipoPermitido === 2 ? ' moto' : '');
                        td.title = e.numeroEspacio + ' - ' + e.estado;
                    }
                    tr.appendChild(td);
                }
                tabla.appendChild(tr);
            }
            const contenedor = document.getElementById('miniMapa');
            contenedor.innerHTML = '';
            contenedor.appendChild(tabla);
        });
}

function actualizar() {
    cargarResumen();
    cargarMiniMapa();
}
actualizar();
setInterval(actualizar, 5000);
</script>
</body>
</html>

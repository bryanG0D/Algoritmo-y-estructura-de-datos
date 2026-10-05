<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Registrar salida</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<% String paginaActual = "salida"; %>
<%@ include file="/WEB-INF/menu.jspf" %>
<main>
    <h2 class="titulo-pagina">Registrar salida</h2>
    <form id="formConsulta">
        <label>Numero de ticket o placa</label>
        <input type="text" id="consulta" placeholder="Ej. 31 o ABC-123" autocomplete="off" required>
        <p class="nota">Si el cliente perdio su ticket, escriba la placa.</p>
        <button type="submit">Buscar</button>
    </form>

    <form id="formCobro" hidden>
        <div id="resumen"></div>
        <label>Metodo de pago</label>
        <select name="metodoPago" id="metodoPago" required>
            <option value="EFECTIVO">Efectivo</option>
            <option value="TARJETA">Tarjeta</option>
            <option value="YAPE">Yape</option>
            <option value="PLIN">Plin</option>
        </select>
        <input type="hidden" name="idTicket" id="idTicket">
        <button type="submit" id="botonCobrar">Cobrar y liberar espacio</button>
    </form>
    <div id="resultado"></div>
</main>
<script src="js/main.js"></script>
<script>
const campoConsulta = document.getElementById('consulta');
const formCobro = document.getElementById('formCobro');
const resultado = document.getElementById('resultado');

// Paso 1: buscar el ticket activo (por numero o por placa) y mostrar cuanto se debe, sin cobrar.
function consultar() {
    const consulta = campoConsulta.value.trim();
    resultado.innerHTML = '';
    formCobro.hidden = true;
    fetch('salida?consulta=' + encodeURIComponent(consulta))
        .then(r => r.json())
        .then(r => {
            if (r.error) { resultado.innerHTML = '<p class="error">' + escaparHtml(r.error) + '</p>'; return; }
            if (!r.encontrado) { resultado.innerHTML = '<p class="aviso">' + escaparHtml(r.mensaje) + '</p>'; return; }
            const t = r.ticket;
            const e = describirEntrada(t.fechaHoraEntrada);
            document.getElementById('resumen').innerHTML =
                '<div class="ficha">' +
                '<div class="ficha-placa">' + escaparHtml(t.placa) + '</div>' +
                '<div>' + [t.nombreTipo, t.marca].filter(Boolean).map(escaparHtml).join(' &middot; ') + '</div>' +
                '<table class="ticket-datos">' +
                '<tr><td>Ticket</td><td>N.&deg; ' + t.idTicket + '</td></tr>' +
                '<tr><td>Espacio</td><td>' + escaparHtml(t.numeroEspacio) + '</td></tr>' +
                '<tr><td>Entrada</td><td>' + fechaHoraCorta(t.fechaHoraEntrada) + ' (hace ' + e.hace + ')</td></tr>' +
                '<tr><td>Tiempo cobrado</td><td>' + t.horas + (t.horas === 1 ? ' hora' : ' horas') + ' x ' + soles(t.precioHora) + '</td></tr>' +
                '</table>' +
                '<div class="total">Total a pagar: ' + soles(t.monto) + '</div>' +
                '</div>';
            document.getElementById('idTicket').value = t.idTicket;
            document.getElementById('botonCobrar').textContent = 'Cobrar ' + soles(t.monto) + ' y liberar espacio';
            formCobro.hidden = false;
        });
}

document.getElementById('formConsulta').addEventListener('submit', function (e) {
    e.preventDefault();
    consultar();
});

// Paso 2: cobrar (el monto final lo calcula el procedimiento almacenado al registrar la salida).
formCobro.addEventListener('submit', function (e) {
    e.preventDefault();
    fetch('salida', { method: 'POST', body: new URLSearchParams(new FormData(formCobro)) })
        .then(r => r.json())
        .then(r => {
            if (r.error) { resultado.innerHTML = '<p class="error">' + escaparHtml(r.error) + '</p>'; return; }
            formCobro.hidden = true;
            campoConsulta.value = '';
            resultado.innerHTML = '<p class="exito">Cobrado ' + soles(r.monto) + '. Espacio ' +
                escaparHtml(r.numeroEspacioLiberado || '') + ' liberado.</p>';
        });
});

// Si se llega desde la busqueda por placa del inicio, el ticket viene en la URL y se consulta solo.
const ticketUrl = new URLSearchParams(location.search).get('ticket');
if (ticketUrl) {
    campoConsulta.value = ticketUrl;
    consultar();
}
</script>
</body>
</html>

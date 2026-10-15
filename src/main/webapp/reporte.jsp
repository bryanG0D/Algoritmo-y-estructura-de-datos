<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Reportes</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<% String paginaActual = "reporte"; %>
<%@ include file="/WEB-INF/menu.jspf" %>
<main class="panel-reporte">
    <h2 class="titulo-pagina">Reporte de ingresos</h2>

    <div class="filtros">
        <button type="button" class="filtro" data-rango="hoy">Hoy</button>
        <button type="button" class="filtro activo" data-rango="semana">Ultimos 7 dias</button>
        <button type="button" class="filtro" data-rango="mes">Este mes</button>
        <button type="button" class="filtro" data-rango="personalizado">Personalizado</button>
    </div>
    <form id="formRango" class="rango-fechas" hidden>
        <label>Desde <input type="date" id="fechaInicio" required></label>
        <label>Hasta <input type="date" id="fechaFin" required></label>
        <button type="submit">Ver</button>
    </form>
    <p class="nota" id="periodo"></p>

    <section class="tarjetas">
        <div class="tarjeta"><span class="dato" id="total">-</span><span class="etiqueta">Total recaudado</span></div>
        <div class="tarjeta"><span class="dato" id="tickets">-</span><span class="etiqueta">Tickets cobrados</span></div>
        <div class="tarjeta"><span class="dato" id="promedio">-</span><span class="etiqueta">Ticket promedio</span></div>
        <div class="tarjeta"><span class="dato" id="mejorDia">-</span><span class="etiqueta" id="mejorDiaMonto">Mejor dia</span></div>
    </section>

    <div class="caja">
        <h2>Ingresos por dia</h2>
        <div id="grafico" class="grafico"></div>
    </div>

    <section class="columnas-inicio">
        <div class="caja">
            <h2>Por tipo de vehiculo</h2>
            <table id="tablaTipo"><thead><tr><th>Tipo</th><th>Tickets</th><th>Ingreso</th><th>%</th></tr></thead><tbody></tbody></table>
        </div>
        <div class="caja">
            <h2>Por metodo de pago</h2>
            <table id="tablaMetodo"><thead><tr><th>Metodo</th><th>Pagos</th><th>Ingreso</th><th>%</th></tr></thead><tbody></tbody></table>
        </div>
    </section>

    <div class="caja">
        <h2>Detalle por dia</h2>
        <table id="tablaDia"><thead><tr><th>Fecha</th><th>Tickets</th><th>Ingreso</th></tr></thead><tbody></tbody></table>
    </div>
</main>
<script src="js/main.js"></script>
<script>
const NOMBRE_METODO = { EFECTIVO: 'Efectivo', TARJETA: 'Tarjeta', YAPE: 'Yape', PLIN: 'Plin' };

// Fecha local en formato AAAA-MM-DD (sin pasar por UTC, que cambiaria el dia de noche)
function aIso(d) {
    return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
}
function diaCorto(iso) {
    const [a, m, d] = iso.split('-');
    return d + '/' + m;
}

function rangoDe(tipo) {
    const hoy = new Date();
    if (tipo === 'hoy') return [aIso(hoy), aIso(hoy)];
    if (tipo === 'mes') return [aIso(new Date(hoy.getFullYear(), hoy.getMonth(), 1)), aIso(hoy)];
    const inicio = new Date(hoy);
    inicio.setDate(hoy.getDate() - 6);
    return [aIso(inicio), aIso(hoy)];
}

function cargarReporte(inicio, fin) {
    document.getElementById('periodo').textContent = 'Periodo: ' + diaCorto(inicio) + ' al ' + diaCorto(fin);
    fetch('reporte?fechaInicio=' + inicio + '&fechaFin=' + fin)
        .then(r => r.json())
        .then(r => {
            if (r.error) { document.getElementById('periodo').innerHTML = '<span class="error">' + escaparHtml(r.error) + '</span>'; return; }
            mostrarResumen(r.datos);
            mostrarGrafico(r.datos, inicio, fin);
            mostrarGrupo('tablaTipo', r.porTipo, n => n);
            mostrarGrupo('tablaMetodo', r.porMetodo, n => NOMBRE_METODO[n] || n);
            mostrarDetalle(r.datos);
        });
}

function mostrarResumen(dias) {
    const total = dias.reduce((s, d) => s + Number(d.ingresoTotal), 0);
    const tickets = dias.reduce((s, d) => s + d.cantidadTickets, 0);
    const mejor = dias.reduce((m, d) => (!m || Number(d.ingresoTotal) > Number(m.ingresoTotal)) ? d : m, null);
    document.getElementById('total').textContent = soles(total);
    document.getElementById('tickets').textContent = tickets;
    document.getElementById('promedio').textContent = tickets ? soles(total / tickets) : '-';
    document.getElementById('mejorDia').textContent = mejor ? diaCorto(mejor.fecha) : '-';
    document.getElementById('mejorDiaMonto').textContent = mejor ? 'Mejor dia (' + soles(mejor.ingresoTotal) + ')' : 'Mejor dia';
}

// Una barra por dia del periodo (los dias sin cobros aparecen en cero).
function mostrarGrafico(dias, inicio, fin) {
    const porFecha = {};
    dias.forEach(d => porFecha[d.fecha] = Number(d.ingresoTotal));
    const fechas = [];
    for (let d = new Date(inicio + 'T00:00'); aIso(d) <= fin && fechas.length < 62; d.setDate(d.getDate() + 1)) {
        fechas.push(aIso(d));
    }
    const maximo = Math.max(1, ...fechas.map(f => porFecha[f] || 0));
    const grafico = document.getElementById('grafico');
    if (dias.length === 0) {
        grafico.innerHTML = '<p class="nota">No hubo cobros en este periodo.</p>';
        return;
    }
    grafico.innerHTML = fechas.map(f => {
        const valor = porFecha[f] || 0;
        return '<div class="barra-col" title="' + diaCorto(f) + ': ' + soles(valor) + '">' +
            '<span class="barra-valor">' + (valor ? 'S/ ' + valor.toFixed(0) : '') + '</span>' +
            '<div class="barra" style="height:' + (valor / maximo * 100) + '%"></div>' +
            '<span class="barra-dia">' + diaCorto(f) + '</span></div>';
    }).join('');
}

function mostrarGrupo(idTabla, filas, nombre) {
    const total = filas.reduce((s, f) => s + Number(f.ingreso), 0);
    const tbody = document.querySelector('#' + idTabla + ' tbody');
    tbody.innerHTML = filas.length ? filas.map(f => {
        const pct = total ? Number(f.ingreso) / total * 100 : 0;
        return '<tr><td>' + escaparHtml(nombre(f.nombre)) + '</td><td>' + f.cantidad + '</td><td>' + soles(f.ingreso) +
            '</td><td><div class="porcentaje"><div style="width:' + pct + '%"></div></div>' + pct.toFixed(0) + '%</td></tr>';
    }).join('') : '<tr><td colspan="4" class="sin-resultados">Sin datos</td></tr>';
}

function mostrarDetalle(dias) {
    const tbody = document.querySelector('#tablaDia tbody');
    tbody.innerHTML = dias.length ? dias.map(d =>
        '<tr><td>' + diaCorto(d.fecha) + '</td><td>' + d.cantidadTickets + '</td><td>' + soles(d.ingresoTotal) + '</td></tr>'
    ).join('') : '<tr><td colspan="3" class="sin-resultados">Sin datos</td></tr>';
}

document.querySelectorAll('.filtro').forEach(boton => boton.addEventListener('click', () => {
    document.querySelectorAll('.filtro').forEach(b => b.classList.remove('activo'));
    boton.classList.add('activo');
    const personalizado = boton.dataset.rango === 'personalizado';
    document.getElementById('formRango').hidden = !personalizado;
    if (!personalizado) cargarReporte(...rangoDe(boton.dataset.rango));
}));

document.getElementById('formRango').addEventListener('submit', function (e) {
    e.preventDefault();
    const inicio = document.getElementById('fechaInicio').value;
    const fin = document.getElementById('fechaFin').value;
    if (inicio > fin) { document.getElementById('periodo').innerHTML = '<span class="error">La fecha inicial es posterior a la final.</span>'; return; }
    cargarReporte(inicio, fin);
});

const [inicioSemana, hoy] = rangoDe('semana');
document.getElementById('fechaInicio').value = inicioSemana;
document.getElementById('fechaFin').value = hoy;
cargarReporte(inicioSemana, hoy);
</script>
</body>
</html>

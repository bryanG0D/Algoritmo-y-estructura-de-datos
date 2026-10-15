<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Vehiculos</title>
    <link rel="stylesheet" href="css/style.css?v=<%= new java.io.File(application.getRealPath("/css/style.css")).lastModified() %>">
</head>
<body>
<% String paginaActual = "vehiculos"; %>
<%@ include file="/WEB-INF/menu.jspf" %>
<main>
    <h2 class="titulo-pagina">Gestion de vehiculos</h2>
    <form id="formVehiculo">
        <input type="hidden" name="idVehiculo" id="idVehiculo">
        <label>Placa</label>
        <input type="text" name="placa" id="placa" maxlength="10" placeholder="Ej. ABC-123" autocomplete="off" required>
        <label>Tipo</label>
        <select name="idTipo" id="idTipo" required></select>
        <label>Marca <span class="opcional">(opcional)</span></label>
        <input type="text" name="marca" id="marca" maxlength="30">
        <button type="submit" id="botonGuardar">Agregar</button>
    </form>
    <div id="mensaje"></div>

    <div class="barra-filtros">
        <label class="campo-filtro">Mostrar
            <select id="filtroEstado">
                <option value="todos">Todos</option>
                <option value="dentro">Dentro del estacionamiento</option>
                <option value="fuera">Fuera</option>
            </select>
        </label>
        <label class="campo-filtro campo-buscar">Buscar
            <input type="text" id="buscar" placeholder="Placa o marca" autocomplete="off">
        </label>
        <button type="button" id="botonExportar" class="boton-excel">Exportar a Excel</button>
    </div>

    <table id="tablaVehiculos">
        <thead>
        <tr><th>Placa</th><th>Marca</th><th>Tipo</th><th>Estado</th><th>Acciones</th></tr>
        </thead>
        <tbody></tbody>
    </table>
</main>
<script src="js/main.js?v=<%= new java.io.File(application.getRealPath("/js/main.js")).lastModified() %>"></script>
<script>
cargarTiposVehiculo('idTipo');
normalizarPlaca(document.getElementById('placa'));

let vehiculos = [], activos = {};

function cargarVehiculos() {
    fetch('vehiculos')
        .then(r => r.json())
        .then(r => {
            vehiculos = r.vehiculos || [];
            activos = r.activos || {};
            mostrarTabla();
        });
}

// Filtra por estado (todos / dentro / fuera) y por el texto buscado en placa o marca.
function mostrarTabla() {
    const texto = document.getElementById('buscar').value.trim().toUpperCase();
    const filtroEstado = document.getElementById('filtroEstado').value;
    const dentro = vehiculos.filter(v => activos[v.idVehiculo]).length;
    const opciones = document.getElementById('filtroEstado').options;
    opciones[0].textContent = 'Todos (' + vehiculos.length + ')';
    opciones[1].textContent = 'Dentro del estacionamiento (' + dentro + ')';
    opciones[2].textContent = 'Fuera (' + (vehiculos.length - dentro) + ')';

    const visibles = vehiculos.filter(v => {
        const estaDentro = !!activos[v.idVehiculo];
        if (filtroEstado === 'dentro' && !estaDentro) return false;
        if (filtroEstado === 'fuera' && estaDentro) return false;
        return !texto || v.placa.includes(texto) || (v.marca || '').toUpperCase().includes(texto);
    });

    const tbody = document.querySelector('#tablaVehiculos tbody');
    tbody.innerHTML = '';
    visibles.forEach(v => {
        const fila = document.createElement('tr');
        fila.innerHTML = [v.placa, v.marca, v.nombreTipo]
            .map(dato => '<td>' + (dato ? escaparHtml(dato) : '-') + '</td>').join('') +
            '<td>' + estadoVehiculo(activos[v.idVehiculo]) + '</td><td></td>';
        const btnEditar = document.createElement('button');
        btnEditar.type = 'button';
        btnEditar.textContent = 'Editar';
        btnEditar.onclick = () => cargarEnFormulario(v);
        fila.lastElementChild.appendChild(btnEditar);
        tbody.appendChild(fila);
    });
    if (visibles.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="sin-resultados">No hay vehiculos que coincidan.</td></tr>';
    }
}

document.getElementById('filtroEstado').addEventListener('change', mostrarTabla);
document.getElementById('buscar').addEventListener('input', mostrarTabla);

// Descarga en Excel exactamente lo que se ve con el filtro y la busqueda actuales.
document.getElementById('botonExportar').addEventListener('click', () => {
    location.href = 'exportar?que=vehiculos&estado=' + document.getElementById('filtroEstado').value +
        '&buscar=' + encodeURIComponent(document.getElementById('buscar').value.trim());
});

// Dentro: muestra su ticket activo (enlace a la salida) para validarlo con el que tiene el cliente.
function estadoVehiculo(activo) {
    if (!activo) return '<span class="estado-fuera">Fuera</span>';
    return '<a class="estado-dentro" href="salida.jsp?ticket=' + activo.idTicket + '">Dentro &middot; Ticket N.&deg; ' +
        activo.idTicket + ' &middot; ' + escaparHtml(activo.numeroEspacio) + '</a>';
}

function cargarEnFormulario(v) {
    document.getElementById('idVehiculo').value = v.idVehiculo;
    document.getElementById('placa').value = v.placa;
    document.getElementById('marca').value = v.marca || '';
    document.getElementById('idTipo').value = v.idTipo;
    document.getElementById('botonGuardar').textContent = 'Actualizar';
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function mostrarMensaje(r) {
    document.getElementById('mensaje').innerHTML =
        '<p class="' + (r.exito ? 'exito' : 'error') + '">' + escaparHtml(r.mensaje) + '</p>';
}

document.getElementById('formVehiculo').addEventListener('submit', function (e) {
    e.preventDefault();
    const idVehiculo = document.getElementById('idVehiculo').value;
    const datos = new URLSearchParams(new FormData(e.target));
    datos.set('accion', idVehiculo ? 'actualizar' : 'crear');

    fetch('vehiculos', { method: 'POST', body: datos })
        .then(r => r.json())
        .then(r => {
            mostrarMensaje(r);
            if (!r.exito) return;
            e.target.reset();
            document.getElementById('idVehiculo').value = '';
            document.getElementById('botonGuardar').textContent = 'Agregar';
            cargarVehiculos();
        });
});

cargarVehiculos();
</script>
</body>
</html>

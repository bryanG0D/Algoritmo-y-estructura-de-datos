<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Vehiculos</title>
    <link rel="stylesheet" href="css/style.css">
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

    <table id="tablaVehiculos">
        <thead>
        <tr><th>Placa</th><th>Marca</th><th>Tipo</th><th>Estado</th><th>Acciones</th></tr>
        </thead>
        <tbody></tbody>
    </table>
</main>
<script src="js/main.js"></script>
<script>
cargarTiposVehiculo('idTipo');
normalizarPlaca(document.getElementById('placa'));

function cargarVehiculos() {
    fetch('vehiculos')
        .then(r => r.json())
        .then(r => {
            const tbody = document.querySelector('#tablaVehiculos tbody');
            tbody.innerHTML = '';
            const activos = r.activos || {};
            (r.vehiculos || []).forEach(v => {
                const fila = document.createElement('tr');
                fila.innerHTML = [v.placa, v.marca, v.nombreTipo]
                    .map(dato => '<td>' + (dato ? escaparHtml(dato) : '-') + '</td>').join('') +
                    '<td>' + estadoVehiculo(activos[v.idVehiculo]) + '</td><td></td>';
                const celdaAcciones = fila.lastElementChild;

                const btnEditar = document.createElement('button');
                btnEditar.type = 'button';
                btnEditar.textContent = 'Editar';
                btnEditar.onclick = () => cargarEnFormulario(v);
                celdaAcciones.appendChild(btnEditar);

                const btnEliminar = document.createElement('button');
                btnEliminar.type = 'button';
                btnEliminar.textContent = 'Eliminar';
                btnEliminar.onclick = () => eliminarVehiculo(v.idVehiculo, v.placa);
                celdaAcciones.appendChild(btnEliminar);

                tbody.appendChild(fila);
            });
        });
}

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
}

function eliminarVehiculo(id, placa) {
    if (!confirm('Eliminar el vehiculo ' + placa + '?')) return;
    const datos = new URLSearchParams();
    datos.append('accion', 'eliminar');
    datos.append('idVehiculo', id);
    fetch('vehiculos', { method: 'POST', body: datos })
        .then(r => r.json())
        .then(r => { mostrarMensaje(r); cargarVehiculos(); });
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

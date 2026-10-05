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
        <label>Modelo <span class="opcional">(opcional)</span></label>
        <input type="text" name="modelo" id="modelo" maxlength="30">
        <label>Color <span class="opcional">(opcional)</span></label>
        <input type="text" name="color" id="color" maxlength="20">
        <button type="submit" id="botonGuardar">Agregar</button>
    </form>

    <table id="tablaVehiculos">
        <thead>
        <tr><th>Placa</th><th>Marca</th><th>Modelo</th><th>Color</th><th>Tipo</th><th>Acciones</th></tr>
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
            (r.vehiculos || []).forEach(v => {
                const fila = document.createElement('tr');
                fila.innerHTML = [v.placa, v.marca, v.modelo, v.color, v.nombreTipo]
                    .map(dato => '<td>' + (dato ? escaparHtml(dato) : '-') + '</td>').join('') + '<td></td>';
                const celdaAcciones = fila.lastElementChild;

                const btnEditar = document.createElement('button');
                btnEditar.type = 'button';
                btnEditar.textContent = 'Editar';
                btnEditar.onclick = () => cargarEnFormulario(v);
                celdaAcciones.appendChild(btnEditar);

                const btnEliminar = document.createElement('button');
                btnEliminar.type = 'button';
                btnEliminar.textContent = 'Eliminar';
                btnEliminar.onclick = () => eliminarVehiculo(v.idVehiculo);
                celdaAcciones.appendChild(btnEliminar);

                tbody.appendChild(fila);
            });
        });
}

function cargarEnFormulario(v) {
    document.getElementById('idVehiculo').value = v.idVehiculo;
    document.getElementById('placa').value = v.placa;
    document.getElementById('marca').value = v.marca || '';
    document.getElementById('modelo').value = v.modelo || '';
    document.getElementById('color').value = v.color || '';
    document.getElementById('idTipo').value = v.idTipo;
    document.getElementById('botonGuardar').textContent = 'Actualizar';
}

function eliminarVehiculo(id) {
    const datos = new URLSearchParams();
    datos.append('accion', 'eliminar');
    datos.append('idVehiculo', id);
    fetch('vehiculos', { method: 'POST', body: datos }).then(cargarVehiculos);
}

document.getElementById('formVehiculo').addEventListener('submit', function (e) {
    e.preventDefault();
    const idVehiculo = document.getElementById('idVehiculo').value;
    const datos = new URLSearchParams(new FormData(e.target));
    datos.set('accion', idVehiculo ? 'actualizar' : 'crear');

    fetch('vehiculos', { method: 'POST', body: datos })
        .then(r => r.json())
        .then(() => {
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

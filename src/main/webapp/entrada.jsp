<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Registrar entrada</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<% String paginaActual = "entrada"; %>
<%@ include file="/WEB-INF/menu.jspf" %>
<main>
    <h2 class="titulo-pagina">Registrar entrada</h2>
    <form id="formEntrada">
        <label>Placa</label>
        <input type="text" name="placa" id="placa" maxlength="10" placeholder="Ej. ABC-123" autocomplete="off" required>
        <p id="avisoPlaca" class="nota"></p>
        <label>Tipo de vehiculo</label>
        <select name="idTipo" id="idTipo" required></select>
        <label>Marca <span class="opcional">(opcional)</span></label>
        <input type="text" name="marca" id="marca" maxlength="30">
        <label>Modelo <span class="opcional">(opcional)</span></label>
        <input type="text" name="modelo" id="modelo" maxlength="30">
        <label>Color <span class="opcional">(opcional)</span></label>
        <input type="text" name="color" id="color" maxlength="20">
        <button type="submit" id="botonRegistrar">Registrar</button>
    </form>
    <div id="resultado"></div>
</main>
<script src="js/main.js"></script>
<script>
cargarTiposVehiculo('idTipo');

const campoPlaca = document.getElementById('placa');
const avisoPlaca = document.getElementById('avisoPlaca');
const botonRegistrar = document.getElementById('botonRegistrar');
let datosAutocompletados = false;
normalizarPlaca(campoPlaca);

// Al salir del campo placa: si el vehiculo ya vino antes, se rellenan sus datos.
campoPlaca.addEventListener('change', function () {
    const placa = campoPlaca.value.trim();
    avisoPlaca.textContent = '';
    botonRegistrar.disabled = false;
    if (datosAutocompletados) {
        ['marca', 'modelo', 'color'].forEach(id => document.getElementById(id).value = '');
        datosAutocompletados = false;
    }
    if (!placa) return;

    fetch('buscar?placa=' + encodeURIComponent(placa))
        .then(r => r.json())
        .then(r => {
            if (!r.encontrado) return;
            const v = r.vehiculo;
            document.getElementById('marca').value = v.marca || '';
            document.getElementById('modelo').value = v.modelo || '';
            document.getElementById('color').value = v.color || '';
            document.getElementById('idTipo').value = v.idTipo;
            datosAutocompletados = true;
            if (r.ticketActivo) {
                avisoPlaca.innerHTML = '<span class="error">Este vehiculo ya esta dentro (espacio ' +
                    escaparHtml(r.numeroEspacio || '') + ', ticket N.&deg; ' + r.ticketActivo.idTicket + ').</span>';
                botonRegistrar.disabled = true;
            } else {
                avisoPlaca.textContent = 'Vehiculo ya registrado: se cargaron sus datos.';
            }
        });
});

document.getElementById('formEntrada').addEventListener('submit', function (e) {
    e.preventDefault();
    const datos = new URLSearchParams(new FormData(e.target));

    fetch('entrada', { method: 'POST', body: datos })
        .then(r => r.json())
        .then(r => {
            const div = document.getElementById('resultado');
            if (r.error) {
                div.innerHTML = '<p class="error">' + escaparHtml(r.error) + '</p>';
            } else if (r.asignado) {
                div.innerHTML = '<p class="exito">Ticket #' + r.idTicket +
                    ' creado. Espacio asignado: ' + escaparHtml(r.numeroEspacio) + '</p>';
            } else {
                div.innerHTML = '<p class="aviso">' + escaparHtml(r.mensaje) + '</p>';
            }
            e.target.reset();
            avisoPlaca.textContent = '';
            datosAutocompletados = false;
            cargarTiposVehiculo('idTipo');
        });
});
</script>
</body>
</html>

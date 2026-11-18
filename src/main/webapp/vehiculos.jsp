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
        <p class="aviso-edicion" id="avisoEdicion" hidden></p>
        <input type="hidden" name="idVehiculo" id="idVehiculo">
        <label>Placa</label>
        <input type="text" name="placa" id="placa" maxlength="7" placeholder="Ej. M4O-634" autocomplete="off" required>
        <label>Tipo</label>
        <select name="idTipo" id="idTipo" required></select>
        <label>Marca <span class="opcional">(opcional)</span></label>
        <input type="text" name="marca" id="marca" maxlength="30">
        <button type="submit" id="botonGuardar">Agregar</button>
        <button type="button" id="botonCancelar" class="boton-secundario" hidden>Cancelar edicion</button>
    </form>

    <div class="barra-filtros">
        <label class="campo-filtro">Mostrar
            <select id="filtroEstado">
                <option value="todos">Todos</option>
                <option value="dentro">Dentro del estacionamiento</option>
                <option value="fuera">Fuera</option>
            </select>
        </label>
        <label class="campo-filtro">Ver hora
            <select id="filtroHora">
                <option value="estado" selected>Según estado</option>
                <option value="ambas">Entrada y salida</option>
            </select>
        </label>
        <label class="campo-filtro campo-buscar">Buscar
            <input type="text" id="buscar" placeholder="Placa o marca" autocomplete="off">
        </label>
        <button type="button" id="botonExportar" class="boton-excel">Exportar a Excel</button>
    </div>

    <table id="tablaVehiculos">
        <thead>
        <tr><th>Placa</th><th>Marca</th><th>Tipo</th><th>Estado</th><th>Hora</th><th>Acciones</th></tr>
        </thead>
        <tbody></tbody>
    </table>
</main>
<script src="js/main.js?v=<%= new java.io.File(application.getRealPath("/js/main.js")).lastModified() %>"></script>
<script>
cargarTiposVehiculo('idTipo');
normalizarPlaca(document.getElementById('placa'));
normalizarPlaca(document.getElementById('buscar'), true);

let vehiculos = [], activos = {}, ultimasVisitas = {}, vehiculoEditando = null;

function cargarVehiculos() {
    fetch('vehiculos')
        .then(r => r.json())
        .then(r => {
            vehiculos = r.vehiculos || [];
            activos = r.activos || {};
            ultimasVisitas = r.ultimasVisitas || {};
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
            '<td>' + estadoVehiculo(activos[v.idVehiculo]) + '</td>' +
            '<td>' + horaVehiculo(v.idVehiculo) + '</td><td></td>';
        const btnEditar = document.createElement('button');
        btnEditar.type = 'button';
        btnEditar.textContent = 'Editar';
        btnEditar.onclick = () => cargarEnFormulario(v);
        fila.lastElementChild.appendChild(btnEditar);
        tbody.appendChild(fila);
    });
    if (visibles.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="sin-resultados">No hay vehiculos que coincidan.</td></tr>';
    }
}

document.getElementById('filtroEstado').addEventListener('change', mostrarTabla);
document.getElementById('filtroHora').addEventListener('change', mostrarTabla);
document.getElementById('buscar').addEventListener('input', mostrarTabla);

// Descarga en Excel exactamente lo que se ve con el filtro y la busqueda actuales.
document.getElementById('botonExportar').addEventListener('click', () => {
    location.href = 'exportar?que=vehiculos&estado=' + document.getElementById('filtroEstado').value +
        '&buscar=' + encodeURIComponent(document.getElementById('buscar').value.trim());
});


function estadoVehiculo(activo) {
    if (!activo) return '<span class="estado-fuera">Fuera</span>';
    return '<a class="estado-dentro" href="salida.jsp?ticket=' + activo.idTicket + '">Dentro &middot; Ticket N.&deg; ' +
        activo.idTicket + ' &middot; ' + escaparHtml(activo.numeroEspacio) + '</a>';
}

// Convierte un ISO "AAAA-MM-DDTHH:MM[:SS]" en un texto corto y legible.
// Devuelve '' si el valor no tiene el formato esperado.
function formatoFecha(iso) {
    if (!iso || typeof iso !== 'string') return '';
    const texto = iso.trim();
    if (texto.length < 16) return '';
    if (texto.charAt(4) !== '-' || texto.charAt(7) !== '-' || texto.charAt(10) !== 'T' || texto.charAt(13) !== ':') return '';
    const anio = texto.substring(0, 4);
    const mes = texto.substring(5, 7);
    const dia = texto.substring(8, 10);
    const hora = texto.substring(11, 16);
    if (!/^\d{4}$/.test(anio) || !/^\d{2}$/.test(mes) || !/^\d{2}$/.test(dia) || !/^\d{2}:\d{2}$/.test(hora)) return '';

    const hoy = new Date();
    const anioHoy = String(hoy.getFullYear());
    const mesHoy = String(hoy.getMonth() + 1).padStart(2, '0');
    const diaHoy = String(hoy.getDate()).padStart(2, '0');

    if (anio === anioHoy && mes === mesHoy && dia === diaHoy) return 'hoy ' + hora;
    if (anio === anioHoy) return dia + '/' + mes + ' ' + hora;
    return dia + '/' + mes + '/' + anio + ' ' + hora;
}

// Arma la celda de hora segun el estado del vehiculo y el modo elegido en "Ver hora".
function horaVehiculo(idVehiculo) {
    const activo = activos[idVehiculo];
    const visita = ultimasVisitas[idVehiculo];
    const modo = document.getElementById('filtroHora').value;
    let principal = '';
    let detalle = '';

    if (activo) {
        const entrada = formatoFecha(activo.entrada);
        principal = entrada ? 'Entró ' + entrada : 'Dentro';
        if (modo === 'ambas') detalle = 'Aún dentro';
    } else if (visita) {
        if (modo === 'ambas') {
            principal = 'Entró ' + formatoFecha(visita.entrada);
            detalle = 'Salió ' + formatoFecha(visita.salida);
        } else {
            principal = 'Salió ' + formatoFecha(visita.salida);
        }
    } else {
        return '<span class="estado-fuera">Sin visitas</span>';
    }

    let html = '<span class="hora-principal">' + escaparHtml(principal) + '</span>';
    if (detalle) html += '<span class="hora-detalle">' + escaparHtml(detalle) + '</span>';
    return html;
}

function cargarEnFormulario(v) {
    vehiculoEditando = v;
    document.getElementById('idVehiculo').value = v.idVehiculo;
    document.getElementById('placa').value = v.placa;
    document.getElementById('marca').value = v.marca || '';
    document.getElementById('idTipo').value = v.idTipo;
    document.getElementById('botonGuardar').textContent = 'Guardar cambios';
    document.getElementById('botonCancelar').hidden = false;
    const avisoEdicion = document.getElementById('avisoEdicion');
    avisoEdicion.textContent = 'Editando el vehiculo ' + v.placa;
    avisoEdicion.hidden = false;
    document.getElementById('formVehiculo').classList.add('editando');
    mostrarAviso('Editando el vehiculo ' + v.placa + '. Corrige los datos y pulsa "Guardar cambios".', 'info');
    window.scrollTo({ top: 0, behavior: 'smooth' });
    document.getElementById('placa').focus();
}

// Vuelve el formulario al modo "Agregar".
function salirDeEdicion() {
    const form = document.getElementById('formVehiculo');
    form.reset();
    form.classList.remove('editando');
    vehiculoEditando = null;
    document.getElementById('idVehiculo').value = '';
    document.getElementById('botonGuardar').textContent = 'Agregar';
    document.getElementById('botonCancelar').hidden = true;
    document.getElementById('avisoEdicion').hidden = true;
}

document.getElementById('botonCancelar').addEventListener('click', salirDeEdicion);

document.getElementById('formVehiculo').addEventListener('submit', function (e) {
    e.preventDefault();
    const idVehiculo = document.getElementById('idVehiculo').value;
    const datos = new URLSearchParams(new FormData(e.target));
    datos.set('accion', idVehiculo ? 'actualizar' : 'crear');

    fetch('vehiculos', { method: 'POST', body: datos })
        .then(r => r.json())
        .then(r => {
            if (!r.exito) { mostrarAviso(r.mensaje, 'error'); return; }
            // Si el vehiculo esta dentro, se ofrece reimprimir su ticket con los datos corregidos.
            const original = vehiculoEditando;
            const cambio = original && (original.placa !== datos.get('placa').toUpperCase() ||
                (original.marca || '') !== datos.get('marca').trim() || String(original.idTipo) !== datos.get('idTipo'));
            const activo = cambio && activos[idVehiculo];
            mostrarAviso(r.mensaje + ' (' + datos.get('placa').toUpperCase() + ')', 'exito',
                activo ? { texto: 'Reimprimir ticket', alHacerClic: () => reimprimirTicket(activo.idTicket) } : null);
            salirDeEdicion();
            cargarVehiculos();
        });
});

cargarVehiculos();
</script>
</body>
</html>

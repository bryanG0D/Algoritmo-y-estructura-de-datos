<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.utp.estacionamiento.modelo.Usuario" %>
<%
    Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Panel - Estacionamiento Inteligente</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<header class="barra-superior">
    <h1>Estacionamiento Inteligente</h1>
    <span>Hola, <%= usuarioSesion.getNombre() %> (<%= usuarioSesion.getRol() %>)</span>
    <a href="logout" class="boton-salir">Salir</a>
</header>
<nav class="menu-principal">
    <a href="entrada.jsp">Registrar entrada</a>
    <a href="salida.jsp">Registrar salida</a>
    <a href="busqueda.jsp">Buscar por placa</a>
    <a href="mapa.jsp">Mapa del estacionamiento</a>
    <a href="vehiculos.jsp">Vehiculos</a>
    <a href="reporte.jsp">Reporte de ingresos</a>
</nav>

<main class="panel-inicio">
    <section class="tarjetas">
        <div class="tarjeta"><span class="dato" id="libresAuto">-</span><span class="etiqueta">Espacios de auto libres</span></div>
        <div class="tarjeta"><span class="dato" id="libresMoto">-</span><span class="etiqueta">Espacios de moto libres</span></div>
        <div class="tarjeta"><span class="dato" id="estacionados">-</span><span class="etiqueta">Vehiculos estacionados</span></div>
        <div class="tarjeta"><span class="dato" id="cobradoHoy">-</span><span class="etiqueta">Cobrado hoy</span></div>
    </section>

    <section class="columnas-inicio">
        <div class="caja">
            <h2>Matriz 5 x 10</h2>
            <div id="miniMapa"></div>
            <p class="nota">
                <span class="celda libre"></span> Libre
                <span class="celda ocupado"></span> Ocupado
                &nbsp; Columna 10 (borde azul): motos y bicicletas
            </p>
        </div>
        <div class="caja">
            <h2>Arbol AVL de placas</h2>
            <p><span class="dato-avl" id="placasEnArbol">-</span> placas indexadas</p>
            <p><span class="dato-avl" id="alturaArbol">-</span> de altura (minimo posible: <span id="alturaMinima">-</span>)</p>
            <p class="nota" id="explicacionAvl"></p>
            <a href="busqueda.jsp">Probar una busqueda &rarr;</a>
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
            document.getElementById('placasEnArbol').textContent = r.placasEnArbol;
            document.getElementById('alturaArbol').textContent = r.alturaArbol;
            document.getElementById('alturaMinima').textContent = r.alturaMinima;
            document.getElementById('explicacionAvl').textContent =
                'Buscar una placa revisa como maximo ' + r.alturaArbol + ' nodos, ' +
                'en lugar de recorrer las ' + r.placasEnArbol + ' placas una por una.';
        });
}

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

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Mapa del estacionamiento</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<header class="barra-superior">
    <h1>Mapa del estacionamiento</h1>
    <a href="index.jsp" class="boton-salir">Volver</a>
</header>
<main>
    <div id="leyenda">
        <span class="celda libre"></span> Libre
        <span class="celda ocupado"></span> Ocupado
        &nbsp; Columna 10 (borde azul): espacios para motos y bicicletas
    </div>
    <div id="matriz"></div>
</main>
<script src="js/main.js"></script>
<script>
function cargarMapa() {
    fetch('mapa')
        .then(r => r.json())
        .then(espacios => {
            const contenedor = document.getElementById('matriz');
            contenedor.innerHTML = '';

            let maxFila = 0, maxColumna = 0;
            espacios.forEach(e => {
                maxFila = Math.max(maxFila, e.fila);
                maxColumna = Math.max(maxColumna, e.columna);
            });

            const tabla = document.createElement('table');
            for (let f = 1; f <= maxFila; f++) {
                const fila = document.createElement('tr');
                for (let c = 1; c <= maxColumna; c++) {
                    const espacio = espacios.find(e => e.fila === f && e.columna === c);
                    const celda = document.createElement('td');
                    if (espacio) {
                        celda.className = 'celda ' + (espacio.estado === 'LIBRE' ? 'libre' : 'ocupado') +
                            (espacio.idTipoPermitido === 2 ? ' moto' : '');
                        celda.title = espacio.numeroEspacio;
                        celda.textContent = espacio.numeroEspacio;
                    }
                    fila.appendChild(celda);
                }
                tabla.appendChild(fila);
            }
            contenedor.appendChild(tabla);
        });
}
cargarMapa();
setInterval(cargarMapa, 5000);
</script>
</body>
</html>

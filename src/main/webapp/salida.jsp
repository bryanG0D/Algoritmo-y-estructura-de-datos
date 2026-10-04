<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Registrar salida</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<header class="barra-superior">
    <h1>Registrar salida</h1>
    <a href="index.jsp" class="boton-salir">Volver</a>
</header>
<main>
    <form id="formSalida">
        <label>Numero de ticket</label>
        <input type="number" name="idTicket" id="idTicket" required>
        <label>Metodo de pago</label>
        <select name="metodoPago" id="metodoPago" required>
            <option value="EFECTIVO">Efectivo</option>
            <option value="TARJETA">Tarjeta</option>
            <option value="YAPE">Yape</option>
            <option value="PLIN">Plin</option>
        </select>
        <button type="submit">Cobrar y liberar espacio</button>
    </form>
    <div id="resultado"></div>
</main>
<script src="js/main.js"></script>
<script>
document.getElementById('formSalida').addEventListener('submit', function (e) {
    e.preventDefault();
    const datos = new URLSearchParams(new FormData(e.target));

    fetch('salida', { method: 'POST', body: datos })
        .then(r => r.json())
        .then(r => {
            const div = document.getElementById('resultado');
            if (r.error) {
                div.innerHTML = '<p class="error">' + r.error + '</p>';
                return;
            }
            let html = '<p class="exito">Monto cobrado: S/ ' + r.monto + '</p>';
            if (r.numeroEspacioLiberado) {
                html += '<p>Espacio ' + r.numeroEspacioLiberado + ' liberado.</p>';
            }
            div.innerHTML = html;
            e.target.reset();
        });
});
</script>
</body>
</html>

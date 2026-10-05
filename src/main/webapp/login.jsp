<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Ingresar - Estacionamiento</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<div class="contenedor-login">
    <h1>Estacionamiento</h1>
    <form id="formLogin">
        <label>Usuario</label>
        <input type="text" name="usuario" id="usuario" required>
        <label>Contrasena</label>
        <input type="password" name="clave" id="clave" required>
        <button type="submit">Ingresar</button>
        <p id="mensajeError" class="error"></p>
    </form>
</div>
<script src="js/main.js"></script>
<script>
document.getElementById('formLogin').addEventListener('submit', function (e) {
    e.preventDefault();
    const datos = new URLSearchParams();
    datos.append('usuario', document.getElementById('usuario').value);
    datos.append('clave', document.getElementById('clave').value);

    fetch('login', { method: 'POST', body: datos })
        .then(r => r.json())
        .then(r => {
            if (r.exito) {
                window.location.href = 'index.jsp';
            } else {
                document.getElementById('mensajeError').textContent = r.mensaje;
            }
        });
});
</script>
</body>
</html>

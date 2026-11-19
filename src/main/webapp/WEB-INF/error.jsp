<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Ocurrió un problema - Estacionamiento</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<div class="contenedor-login">
    <h1>Ocurrió un problema</h1>
    <p>No pudimos completar la acción. Intenta de nuevo en unos segundos.</p>
    <p><a href="<%= request.getContextPath() %>/index.jsp">Volver al inicio</a></p>
</div>
</body>
</html>

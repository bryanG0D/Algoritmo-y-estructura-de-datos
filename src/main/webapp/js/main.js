// Funciones auxiliares compartidas por todas las paginas JSP.

function cargarTiposVehiculo(idSelect) {
    fetch('tipos')
        .then(r => r.json())
        .then(r => {
            const select = document.getElementById(idSelect);
            select.innerHTML = '';
            (r.tipos || []).forEach(t => {
                const opcion = document.createElement('option');
                opcion.value = t.idTipo;
                opcion.textContent = t.nombre + ' (S/ ' + t.precioHora + '/hora)';
                select.appendChild(opcion);
            });
        });
}

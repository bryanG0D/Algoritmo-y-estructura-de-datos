// Funciones auxiliares compartidas por todas las paginas JSP.

// Si la sesion vence (30 min sin uso), FiltroSesion responde 401 a los
// Servlets. Envolvemos fetch una sola vez para que cualquier pagina vuelva
// al login en ese caso, en lugar de mostrar un error.
const fetchOriginal = window.fetch;
window.fetch = function (...args) {
    return fetchOriginal.apply(this, args).then(r => {
        if (r.status === 401) {
            window.location.href = 'login.jsp';
        }
        return r;
    });
};

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

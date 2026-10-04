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

// Escapa texto ingresado por usuarios antes de insertarlo como HTML.
function escaparHtml(texto) {
    return String(texto ?? '').replace(/[&<>"']/g, c =>
        ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

// "2026-10-03T21:36:38" -> "21:36" (o "03/10 21:36" si no es de hoy) y tiempo transcurrido.
function describirEntrada(fechaIso) {
    const entrada = new Date(fechaIso);
    const hoy = new Date();
    const hora = entrada.toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' });
    const mismoDia = entrada.toDateString() === hoy.toDateString();
    const cuando = mismoDia ? 'las ' + hora
        : entrada.toLocaleDateString('es-PE', { day: '2-digit', month: '2-digit' }) + ' a las ' + hora;
    const minutos = Math.max(0, Math.floor((hoy - entrada) / 60000));
    const h = Math.floor(minutos / 60), m = minutos % 60;
    const hace = h > 0 ? h + ' h ' + m + ' min' : m + ' min';
    return { cuando, hace };
}

// Ficha del vehiculo para la respuesta de /buscar (usada en inicio y en busqueda).
function fichaVehiculo(r) {
    if (r.error) return '<p class="error">' + escaparHtml(r.error) + '</p>';
    if (!r.encontrado) return '<p class="aviso">' + escaparHtml(r.mensaje) + '</p>';
    const v = r.vehiculo;
    let html = '<div class="ficha">' +
        '<div class="ficha-placa">' + escaparHtml(v.placa) + '</div>' +
        '<div>' + escaparHtml(v.marca) + ' ' + escaparHtml(v.modelo) + ' &middot; ' + escaparHtml(v.color) +
        (r.nombreTipo ? ' &middot; ' + escaparHtml(r.nombreTipo) : '') + '</div>';
    if (r.ticketActivo) {
        const e = describirEntrada(r.ticketActivo.fechaHoraEntrada);
        html += '<p class="exito">Estacionado en el espacio ' + escaparHtml(r.numeroEspacio || '') +
            ' desde ' + e.cuando + ' (hace ' + e.hace + ').</p>' +
            '<p>Ticket N.&deg; ' + r.ticketActivo.idTicket + '</p>' +
            '<a class="boton" href="salida.jsp?ticket=' + r.ticketActivo.idTicket + '">Registrar salida</a>';
    } else {
        html += '<p class="aviso">No se encuentra en el estacionamiento.</p>';
    }
    return html + '</div>';
}

// Detalle tecnico plegado: como se encontro la placa en el Arbol AVL (para la exposicion).
function detalleTecnico(r) {
    if (r.comparaciones === undefined) return '';
    const ultimo = r.recorrido.length - 1;
    const pasos = r.recorrido.map((p, i) =>
        '<span class="nodo-avl' + (r.encontrado && i === ultimo ? ' encontrado' : '') + '">' + escaparHtml(p) + '</span>').join(' &rarr; ');
    return '<details class="detalle-tecnico"><summary>Detalle tecnico de la busqueda</summary>' +
        '<p>Busqueda en Arbol AVL: ' + r.comparaciones + ' comparaciones entre ' +
        r.totalPlacas + ' placas (altura del arbol: ' + r.alturaArbol + ').</p>' +
        '<p class="recorrido-avl">' + (pasos || 'Arbol vacio') + '</p>' +
        '<p class="nota">Recorriendo una lista de inicio a fin podrian ser hasta ' +
        r.totalPlacas + ' comparaciones.</p></details>';
}

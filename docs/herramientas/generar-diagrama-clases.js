// Genera docs/entregables/diagrama-clases.drawio leyendo el codigo Java.
// Uso (desde la raiz del proyecto):  node docs/herramientas/generar-diagrama-clases.js .
// Con LUCID_OUT=archivo.json tambien genera el JSON para importar en Lucidchart.
const fs = require('fs');
const path = require('path');

const RAIZ = process.argv[2];
const SRC = path.join(RAIZ, 'src/main/java/com/utp/estacionamiento');
const SALIDA = path.join(RAIZ, 'docs/entregables/diagrama-clases.drawio');

// ---------- Lectura y parseo ----------
function limpiar(txt) {
    return txt
        .replace(/"(?:\\.|[^"\\])*"/g, '""')
        .replace(/'(?:\\.|[^'\\])'/g, "''")
        .replace(/\/\*[\s\S]*?\*\//g, '')
        .replace(/\/\/.*$/gm, '')
        .replace(/@\w+(\([^)]*\))?/g, '');
}

function cuerpoDesde(txt, idxLlave) {
    let d = 0;
    for (let i = idxLlave; i < txt.length; i++) {
        if (txt[i] === '{') d++;
        else if (txt[i] === '}') { d--; if (d === 0) return txt.slice(idxLlave + 1, i); }
    }
    throw new Error('llaves desbalanceadas');
}

function miembros(cuerpo) {
    const res = [];
    let buf = '';
    for (let i = 0; i < cuerpo.length; i++) {
        const ch = cuerpo[i];
        if (ch === '{') {
            const interior = cuerpoDesde(cuerpo, i);
            res.push({ cabecera: buf.trim().replace(/\s+/g, ' '), cuerpo: interior });
            i += interior.length + 1;
            buf = '';
        } else if (ch === ';') {
            res.push({ cabecera: buf.trim().replace(/\s+/g, ' '), cuerpo: null });
            buf = '';
        } else buf += ch;
    }
    return res.filter(m => m.cabecera);
}

function partirComas(s) {
    const out = []; let d = 0, b = '';
    for (const ch of s) {
        if (ch === '<') d++; else if (ch === '>') d--;
        if (ch === ',' && d === 0) { out.push(b.trim()); b = ''; } else b += ch;
    }
    if (b.trim()) out.push(b.trim());
    return out;
}

const MODIF = ['public', 'private', 'protected', 'static', 'final', 'synchronized', 'abstract', 'transient', 'volatile', 'default'];

function visibilidad(mods, esInterfaz) {
    if (mods.includes('public') || esInterfaz) return '+';
    if (mods.includes('private')) return '-';
    if (mods.includes('protected')) return '#';
    return '~';
}

function tipoLimpio(t) { return t.replace(/\s*,\s*/g, ', ').replace(/\s+(?=[<\[])/g, ''); }

function parsearClase(nombre, cabeceraDecl, cuerpo, paquete, esInterfaz) {
    const c = { nombre, paquete, esInterfaz, atributos: [], metodos: [], extiende: null, implementa: [], anidadas: [], texto: cuerpo };
    const ext = cabeceraDecl.match(/extends\s+([\w.]+)/);
    if (ext) c.extiende = ext[1];
    const imp = cabeceraDecl.match(/implements\s+([\w.,\s]+)/);
    if (imp) c.implementa = imp[1].split(',').map(s => s.trim()).filter(Boolean);
    c.esFinal = /\bfinal\b/.test(cabeceraDecl);

    for (const m of miembros(cuerpo)) {
        const h = m.cabecera;
        const decl = h.match(/^(.*\b)(class|interface)\s+(\w+)(.*)$/);
        if (decl && m.cuerpo !== null) {
            c.anidadas.push(parsearClase(nombre + '.' + decl[3], h, m.cuerpo, paquete, decl[2] === 'interface'));
            continue;
        }
        if (h === 'static') continue; // bloque estatico
        if (h.includes('(') && !/=/.test(h.split('(')[0])) {
            const mm = h.match(/^(.*?)(\w+)\s*\((.*)\)\s*(throws\s+.*)?$/);
            if (!mm) continue;
            const antes = mm[1].trim().split(' ').filter(Boolean);
            const mods = antes.filter(t => MODIF.includes(t));
            const tipo = tipoLimpio(antes.filter(t => !MODIF.includes(t)).join(' '));
            const params = partirComas(mm[3]).map(p => {
                const t = p.replace(/\bfinal\s+/, '').trim().split(' ');
                const n = t.pop();
                return n + ': ' + tipoLimpio(t.join(' '));
            });
            const esConstructor = mm[2] === nombre.split('.').pop() && !tipo;
            c.metodos.push({
                vis: visibilidad(mods, esInterfaz), estatico: mods.includes('static'),
                nombre: mm[2], params, tipo: esConstructor ? null : tipo, esConstructor,
            });
        } else {
            const sinInit = h.split('=')[0].trim();
            const toks = sinInit.split(' ').filter(Boolean);
            const mods = toks.filter(t => MODIF.includes(t));
            const resto = toks.filter(t => !MODIF.includes(t));
            if (resto.length < 2) continue;
            const n = resto.pop();
            c.atributos.push({
                vis: visibilidad(mods, false), estatico: mods.includes('static'),
                constante: mods.includes('static') && mods.includes('final'),
                nombre: n, tipo: tipoLimpio(resto.join(' ')),
            });
        }
    }
    return c;
}

const clases = {};
for (const paq of fs.readdirSync(SRC)) {
    const dir = path.join(SRC, paq);
    if (!fs.statSync(dir).isDirectory()) continue;
    for (const f of fs.readdirSync(dir).filter(f => f.endsWith('.java'))) {
        const txt = limpiar(fs.readFileSync(path.join(dir, f), 'utf8'));
        const decl = txt.match(/(?:public\s+)?(?:final\s+)?(class|interface)\s+(\w+)[^{]*/);
        const idx = txt.indexOf('{', decl.index);
        const c = parsearClase(decl[2], decl[0], cuerpoDesde(txt, idx), paq, decl[1] === 'interface');
        clases[c.nombre] = c;
        for (const a of c.anidadas) clases[a.nombre] = a;
    }
}

// ---------- Relaciones ----------
const nombresProyecto = Object.keys(clases).filter(n => !n.includes('.'));
function tiposEn(t) { return nombresProyecto.filter(n => new RegExp('\\b' + n + '\\b').test(t)); }

const relaciones = []; // {de, a, tipo}
for (const c of Object.values(clases)) {
    if (c.extiende) relaciones.push({ de: c.nombre, a: c.extiende, tipo: 'herencia' });
    for (const i of c.implementa) relaciones.push({ de: c.nombre, a: i, tipo: c.esInterfaz ? 'herencia' : 'realizacion' });
    const asociados = new Set();
    for (const a of c.atributos) for (const t of tiposEn(a.tipo)) {
        if (t === c.nombre.split('.')[0] && c.nombre.includes('.')) continue;
        asociados.add(t);
    }
    for (const t of asociados) relaciones.push({ de: c.nombre, a: t, tipo: 'asociacion' });
    for (const t of tiposEn(c.texto)) {
        if (t === c.nombre || asociados.has(t) || c.implementa.includes(t) || c.extiende === t) continue;
        if (c.nombre.startsWith(t + '.')) continue;
        relaciones.push({ de: c.nombre, a: t, tipo: 'dependencia' });
    }
}
// Composicion: el servicio crea y es duenio de sus estructuras; el arbol de sus nodos.
const COMPOSICION = [['EstacionamientoService', 'ArbolAVL'], ['EstacionamientoService', 'MatrizEstacionamiento'], ['ArbolAVL', 'NodoAVL']];
for (const r of relaciones) if (COMPOSICION.some(([d, a]) => r.de === d && r.a === a)) r.tipo = 'composicion';
for (const n of Object.keys(clases).filter(n => n.includes('.')))
    relaciones.push({ de: n.split('.')[0], a: n, tipo: 'anidada' });

// ---------- Texto UML ----------
const esc = s => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
const xmlEsc = s => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');

const COLAPSAR = c => c.paquete === 'modelo' || ['ReporteIngresoDia', 'ResultadoEntrada', 'ResultadoSalida', 'ResultadoBusquedaAVL'].includes(c.nombre);

function lineasAtributos(c) {
    return c.atributos.map(a => {
        let s = esc(`${a.vis} ${a.constante ? a.nombre : a.nombre}: ${a.tipo}`);
        return a.estatico ? `<u>${s}</u>` : s;
    });
}
function lineasMetodos(c) {
    let ms = c.metodos;
    const out = [];
    let ocultos = 0;
    if (COLAPSAR(c)) {
        const triv = ms.filter(m => /^(get|set|is)[A-Z]/.test(m.nombre));
        ocultos = triv.length;
        ms = ms.filter(m => !triv.includes(m));
    }
    const MAX = 64;
    for (const m of ms) {
        const pre = (m.esConstructor ? '«create» ' : '') + `${m.vis} ${m.nombre}(`;
        const post = ')' + (m.tipo ? `: ${m.tipo}` : '');
        let piezas = [pre + m.params.join(', ') + post];
        if (piezas[0].length > MAX) {
            // firma larga: un parametro por linea, con sangria
            piezas = [pre + (m.params[0] || '') + (m.params.length > 1 ? ',' : post)];
            m.params.slice(1).forEach((p, i, arr) => piezas.push('    ' + p + (i === arr.length - 1 ? post : ',')));
        }
        for (const p of piezas) {
            const s = esc(p);
            out.push(m.estatico ? `<u>${s}</u>` : s);
        }
    }
    if (ocultos) {
        const hayS = c.metodos.some(m => /^set[A-Z]/.test(m.nombre));
        out.push(esc(`+ get${hayS ? '/set' : ''}...() (${ocultos} metodos de acceso)`));
    }
    return out;
}

const COLORES = {
    modelo: ['#d5e8d4', '#82b366'], estructuras: ['#dae8fc', '#6c8ebf'], dao: ['#fff2cc', '#d6b656'],
    conexion: ['#fff2cc', '#d6b656'], servicio: ['#f8cecc', '#b85450'], servlet: ['#e1d5e7', '#9673a6'],
    util: ['#f5f5f5', '#666666'], externo: ['#ffffff', '#999999'],
};


// ---------- Paginas: grilla y relaciones elegidas a mano ----------
// Cada celda: [fila, columna, nombre o lista de nombres apilados, opciones]
// Cada relacion: [de, a, tipo, { lados: ['top','top'] }]. Si no se indican
// lados: misma fila -> izquierda/derecha; si no -> abajo/arriba.
const COMPACTA = { compacta: true };
const PAGINAS = [
    {
        nombre: '1. Vista general',
        titulo: 'Diagrama de clases - Vista general por paquetes',
        sub: 'com.utp.estacionamiento. Flecha punteada: un paquete usa clases de otro.',
        nota: 'Todos los paquetes usan las clases de modelo; solo se dibujan las flechas de dao y estructuras hacia modelo.',
        paquetes: true,
        celdas: [[0, 0, 'servlet'], [0, 2, 'util'], [1, 1, 'servicio'], [2, 0, 'estructuras'], [2, 2, 'dao'], [3, 1, 'modelo'], [3, 2, 'conexion']],
        relaciones: [
            ['servlet', 'util', 'dependencia'], ['servlet', 'servicio', 'dependencia'], ['servlet', 'estructuras', 'dependencia'],
            ['servlet', 'dao', 'dependencia'], ['util', 'servicio', 'dependencia'], ['servicio', 'estructuras', 'dependencia'],
            ['servicio', 'dao', 'dependencia'], ['estructuras', 'modelo', 'dependencia'], ['dao', 'modelo', 'dependencia'],
            ['dao', 'conexion', 'dependencia'],
        ],
    },
    {
        nombre: '2. Estructuras y modelo',
        titulo: 'Estructuras de datos y modelo',
        sub: 'Arbol AVL (busqueda por placa en O(log n)) y Matriz 5x10 (asignacion de espacios). El modelo refleja las 6 tablas de MySQL.',
        nota: 'ArbolAVL tambien usa Vehiculo al insertar y buscar (dependencia no dibujada). Las clases del modelo se relacionan por ids, igual que las tablas del diagrama E-R.',
        celdas: [
            [0, 0, 'ArbolAVL'], [0, 1, 'NodoAVL'], [0, 2, 'ResultadoBusquedaAVL'], [0, 3, 'MatrizEstacionamiento'],
            [1, 0, 'TipoVehiculo'], [1, 1, 'Vehiculo'], [1, 3, 'EspacioEstacionamiento'],
            [2, 0, 'Ticket'], [2, 1, 'Pago'], [2, 2, 'Usuario'], [2, 3, 'Serializable', { estereotipo: '«interface»', nota: 'java.io' }],
        ],
        relaciones: [
            ['ArbolAVL', 'NodoAVL', 'composicion'], ['NodoAVL', 'NodoAVL', 'asociacion'],
            ['ArbolAVL', 'ResultadoBusquedaAVL', 'dependencia', { lados: ['top', 'top'] }],
            ['NodoAVL', 'Vehiculo', 'asociacion'], ['ResultadoBusquedaAVL', 'Vehiculo', 'asociacion'],
            ['MatrizEstacionamiento', 'EspacioEstacionamiento', 'asociacion'], ['Usuario', 'Serializable', 'realizacion'],
        ],
    },
    {
        nombre: '3. Patron DAO',
        titulo: 'Capa de acceso a datos - Patron DAO',
        sub: 'Cada entidad tiene una interfaz DAO y su implementacion JDBC. TicketDAOImpl y PagoDAOImpl llaman a los procedimientos almacenados.',
        nota: 'Las interfaces TicketDAO y PagoDAO tambien devuelven ResultadoEntrada, ResultadoSalida y ReporteIngresoDia.',
        celdas: [
            [0, 0, 'VehiculoDAO'], [0, 1, 'TipoVehiculoDAO'], [0, 2, 'UsuarioDAO'], [0, 3, 'EspacioEstacionamientoDAO'], [0, 4, 'TicketDAO'], [0, 5, 'PagoDAO'],
            [1, 0, 'VehiculoDAOImpl'], [1, 1, 'TipoVehiculoDAOImpl'], [1, 2, 'UsuarioDAOImpl'], [1, 3, 'EspacioEstacionamientoDAOImpl'], [1, 4, 'TicketDAOImpl'], [1, 5, 'PagoDAOImpl'],
            [2, 1, 'ConexionBD'], [2, 3, 'ResultadoEntrada'], [2, 4, 'ResultadoSalida'], [2, 5, 'ReporteIngresoDia'],
        ],
        relaciones: [
            ...['Vehiculo', 'TipoVehiculo', 'Usuario', 'EspacioEstacionamiento', 'Ticket', 'Pago'].flatMap(e => [
                [e + 'DAOImpl', e + 'DAO', 'realizacion'], [e + 'DAOImpl', 'ConexionBD', 'dependencia']]),
            ['TicketDAOImpl', 'ResultadoEntrada', 'dependencia'], ['TicketDAOImpl', 'ResultadoSalida', 'dependencia'],
            ['PagoDAOImpl', 'ReporteIngresoDia', 'dependencia'],
        ],
    },
    {
        nombre: '4. Servlets',
        titulo: 'Capa web - Servlets (controladores)',
        sub: 'Todos heredan de HttpServlet. Los de la izquierda usan un DAO directamente; los de la derecha usan EstacionamientoService.',
        nota: 'Todos los Servlets usan GsonProvider para responder JSON (no se dibuja). El detalle de los DAO esta en la pagina 3 y el de EstacionamientoService en la 5.',
        celdas: [
            [0, 4, 'HttpServlet', { nota: 'javax.servlet.http' }],
            [1, 0, 'LoginServlet'], [1, 1, 'TipoVehiculoServlet'], [1, 2, 'VehiculoServlet'], [1, 3, 'ReporteServlet'], [1, 4, 'LogoutServlet'],
            [1, 5, 'EntradaServlet'], [1, 6, 'SalidaServlet'], [1, 7, 'BusquedaServlet'], [1, 8, 'MapaServlet'], [1, 9, 'ResumenServlet'],
            [2, 0, 'UsuarioDAO', COMPACTA], [2, 1, 'TipoVehiculoDAO', COMPACTA], [2, 2, 'VehiculoDAO', COMPACTA], [2, 3, 'PagoDAO', COMPACTA],
            [2, 7, 'EstacionamientoService', COMPACTA],
        ],
        relaciones: [
            ...['Login', 'TipoVehiculo', 'Vehiculo', 'Reporte', 'Logout', 'Entrada', 'Salida', 'Busqueda', 'Mapa', 'Resumen']
                .map(s => [s + 'Servlet', 'HttpServlet', 'herencia']),
            ['LoginServlet', 'UsuarioDAO', 'asociacion'], ['TipoVehiculoServlet', 'TipoVehiculoDAO', 'asociacion'],
            ['VehiculoServlet', 'VehiculoDAO', 'asociacion'], ['ReporteServlet', 'PagoDAO', 'asociacion'],
            ...['Entrada', 'Salida', 'Busqueda', 'Mapa', 'Resumen'].map(s => [s + 'Servlet', 'EstacionamientoService', 'dependencia']),
        ],
    },
    {
        nombre: '5. Servicio y utilidades',
        titulo: 'Servicio central y utilidades',
        sub: 'AppContextListener crea un unico EstacionamientoService al arrancar; este integra el Arbol AVL, la Matriz y los DAO.',
        nota: 'El detalle de ArbolAVL y MatrizEstacionamiento esta en la pagina 2 y el de los DAO en la 3. FiltroSesion exige sesion en todas las paginas.',
        celdas: [
            [0, 0, 'ServletContextListener', { estereotipo: '«interface»', nota: 'javax.servlet' }], [0, 1, 'AppContextListener'],
            [0, 3, 'FiltroSesion'], [0, 4, 'Filter', { estereotipo: '«interface»', nota: 'javax.servlet' }], [0, 5, 'GsonProvider'],
            [1, 0, ['EstacionamientoService.ResultadoRegistro', 'EstacionamientoService.ResultadoCobro']], [1, 1, 'EstacionamientoService'], [1, 2, 'CompatibilidadEspacio'],
            [2, 0, 'ArbolAVL', COMPACTA], [2, 1, 'MatrizEstacionamiento', COMPACTA], [2, 2, 'VehiculoDAO', COMPACTA], [2, 3, 'EspacioEstacionamientoDAO', COMPACTA],
            [2, 4, 'TicketDAO', COMPACTA], [2, 5, 'PagoDAO', COMPACTA], [2, 6, 'TipoVehiculoDAO', COMPACTA],
        ],
        relaciones: [
            ['AppContextListener', 'ServletContextListener', 'realizacion'], ['FiltroSesion', 'Filter', 'realizacion'],
            ['AppContextListener', 'EstacionamientoService', 'dependencia'],
            ['EstacionamientoService', 'EstacionamientoService.ResultadoRegistro', 'anidada'],
            ['EstacionamientoService', 'EstacionamientoService.ResultadoCobro', 'anidada'],
            ['EstacionamientoService', 'CompatibilidadEspacio', 'dependencia'],
            ['EstacionamientoService', 'ArbolAVL', 'composicion'], ['EstacionamientoService', 'MatrizEstacionamiento', 'composicion'],
            ...['VehiculoDAO', 'EspacioEstacionamientoDAO', 'TicketDAO', 'PagoDAO', 'TipoVehiculoDAO'].map(d => ['EstacionamientoService', d, 'asociacion']),
        ],
    },
];

// Avisa si una relacion dibujada no existe en el codigo (para no inventar flechas).
for (const p of PAGINAS.filter(p => !p.paquetes)) {
    for (const [de, a, tipo] of p.relaciones) {
        if (de === a) continue;
        const existe = relaciones.some(r => r.de === de && r.a === a) || (tipo === 'herencia' && clases[de] && clases[de].extiende === a);
        if (!existe) console.warn(`AVISO ${p.nombre}: ${de} -> ${a} (${tipo}) no aparece en el codigo`);
    }
}

// ---------- Geometria comun ----------
const GAP_X = 70, GAP_Y = 130, X0 = 40, Y0 = 130;

// medir(nombre, op) -> {w, h}. Devuelve posiciones {nombre: {x, y, w, h, fila, col}}
function ubicar(pagina, medir) {
    const colW = {}, filaH = {}, medidas = [];
    for (const [f, c, nombres, op = {}] of pagina.celdas) {
        const lista = Array.isArray(nombres) ? nombres : [nombres];
        const ms = lista.map(n => ({ n, ...medir(n, op) }));
        const w = Math.max(...ms.map(m => m.w));
        const h = ms.reduce((s, m) => s + m.h, 0) + 40 * (ms.length - 1);
        colW[c] = Math.max(colW[c] || 0, w);
        filaH[f] = Math.max(filaH[f] || 0, h);
        medidas.push({ f, c, ms, h });
    }
    const maxC = Math.max(...Object.keys(colW).map(Number)), maxF = Math.max(...Object.keys(filaH).map(Number));
    const colX = [], filaY = [];
    for (let c = 0, x = X0; c <= maxC; c++) { colX[c] = x; x += (colW[c] || 200) + GAP_X; }
    for (let f = 0, y = Y0; f <= maxF; f++) { filaY[f] = y; y += (filaH[f] || 100) + GAP_Y; }
    const pos = {};
    for (const { f, c, ms } of medidas) {
        let y = filaY[f];
        for (const m of ms) {
            pos[m.n] = { x: Math.round(colX[c] + (colW[c] - m.w) / 2), y, w: m.w, h: m.h, fila: f, col: c };
            y += m.h + 40;
        }
    }
    const anchoTotal = colX[maxC] + colW[maxC];
    const altoTotal = filaY[maxF] + filaH[maxF];
    return { pos, anchoTotal, altoTotal };
}

// Decide lados y reparte los puntos de conexion en cada lado.
function puertos(pagina, pos) {
    const rels = pagina.relaciones.map(([de, a, tipo, op = {}]) => {
        const A = pos[de], B = pos[a];
        let lados = op.lados;
        if (!lados) {
            if (de === a) lados = ['right', 'right'];
            else if (A.fila === B.fila && A.col === B.col) lados = A.y < B.y ? ['bottom', 'top'] : ['top', 'bottom'];
            else if (A.fila === B.fila) lados = A.x < B.x ? ['right', 'left'] : ['left', 'right'];
            else lados = A.fila < B.fila ? ['bottom', 'top'] : ['top', 'bottom'];
        }
        return { de, a, tipo, lados, frac: [0.5, 0.5] };
    });
    const grupos = {};
    rels.forEach((r, i) => {
        [[r.de, r.lados[0], r.a, 0], [r.a, r.lados[1], r.de, 1]].forEach(([n, lado, otro, k]) => {
            const key = n + '|' + lado;
            (grupos[key] = grupos[key] || []).push({ i, k, otro, lado });
        });
    });
    for (const lista of Object.values(grupos)) {
        const horiz = ['top', 'bottom'].includes(lista[0].lado);
        lista.sort((p, q) => {
            const P = pos[p.otro], Q = pos[q.otro];
            return horiz ? (P.x + P.w / 2) - (Q.x + Q.w / 2) : (P.y + P.h / 2) - (Q.y + Q.h / 2);
        });
        lista.forEach((p, j) => { rels[p.i].frac[p.k] = +((j + 1) / (lista.length + 1)).toFixed(3); });
    }
    // autorreferencia: dos puntos distintos en el mismo lado
    for (const r of rels) if (r.de === r.a) r.frac = [0.3, 0.7];
    return rels;
}

const punto = (lado, f) => ({ top: [f, 0], bottom: [f, 1], left: [0, f], right: [1, f] }[lado]);

const LEYENDA = [
    ['herencia', 'Herencia (extends)'], ['realizacion', 'Implementa (implements)'], ['asociacion', 'Asociacion (atributo)'],
    ['composicion', 'Composicion (crea y contiene)'], ['dependencia', 'Dependencia (usa)'], ['anidada', 'Clase interna'],
];

// ---------- Salida draw.io ----------
let idSeq = 2;
const PX = 6.6, LH = 17, HEAD = 30;
const anchoTexto = lineas => Math.max(...lineas.map(l => l.replace(/<[^>]+>/g, '').replace(/&\w+;/g, 'x').length)) * PX + 16;

function medirDrawio(nombre, op) {
    const c = clases[nombre];
    if (!c || op.compacta) {
        const ester = op.estereotipo || (c && c.esInterfaz ? '«interface»' : '');
        return { w: Math.max(150, anchoTexto([nombre, op.nota || '']) + 16), h: 30 + (ester ? 14 : 0) + 14 };
    }
    const atr = lineasAtributos(c), met = lineasMetodos(c), simple = nombre.split('.').pop();
    const head = HEAD + (c.esInterfaz || nombre.includes('.') ? 12 : 0);
    const hA = c.esInterfaz && !atr.length ? 0 : Math.max(1, atr.length) * LH + 8;
    const hM = Math.max(1, met.length) * LH + 8;
    return { w: Math.max(170, anchoTexto([...atr, ...met, simple]), nombre.includes('.') ? 250 : 0), h: head + hA + (hA ? 8 : 0) + hM };
}

function claseDrawio(celdas, id, nombre, p, op) {
    const c = clases[nombre];
    const { x, y, w, h } = p;
    if (!c || op.compacta) {
        const [fill, stroke] = c ? COLORES[c.paquete] : COLORES.externo;
        const ester = op.estereotipo || (c && c.esInterfaz ? '«interface»' : '');
        const pie = op.nota || (c ? '(detalle en pagina ' + (c.paquete === 'estructuras' ? 2 : c.paquete === 'servicio' ? 5 : 3) + ')' : '');
        const titulo = (ester ? ester + '<br>' : '') + (c && c.esInterfaz ? `<i><b>${nombre}</b></i>` : `<b>${nombre}</b>`) + `<br><font style="font-size:10px">${pie}</font>`;
        celdas.push(`<mxCell id="${id}" value="${xmlEsc(titulo)}" style="rounded=0;whiteSpace=wrap;html=1;fillColor=${fill};strokeColor=${stroke};${c ? '' : 'dashed=1;'}fontSize=11;" vertex="1" parent="1"><mxGeometry x="${x}" y="${y}" width="${w}" height="${h}" as="geometry"/></mxCell>`);
        return;
    }
    const [fill, stroke] = COLORES[c.paquete];
    const atr = lineasAtributos(c), met = lineasMetodos(c), simple = nombre.split('.').pop();
    let titulo = c.esInterfaz ? `«interface»<br><i><b>${simple}</b></i>` : `<b>${simple}</b>`;
    if (nombre.includes('.')) titulo = `<b>${simple}</b><br><font style="font-size:10px">(clase interna de ${nombre.split('.')[0]})</font>`;
    const head = HEAD + (c.esInterfaz || nombre.includes('.') ? 12 : 0);
    const hA = c.esInterfaz && !atr.length ? 0 : Math.max(1, atr.length) * LH + 8;
    const hM = Math.max(1, met.length) * LH + 8;
    celdas.push(`<mxCell id="${id}" value="${xmlEsc(titulo)}" style="swimlane;fontStyle=0;align=center;verticalAlign=top;childLayout=stackLayout;horizontal=1;startSize=${head};horizontalStack=0;resizeParent=1;resizeParentMax=0;resizeLast=0;collapsible=0;marginBottom=0;html=1;fillColor=${fill};strokeColor=${stroke};fontSize=12;" vertex="1" parent="1"><mxGeometry x="${x}" y="${y}" width="${w}" height="${h}" as="geometry"/></mxCell>`);
    const txt = 'text;strokeColor=none;fillColor=none;align=left;verticalAlign=top;spacingLeft=6;spacingRight=4;overflow=hidden;rotatable=0;html=1;whiteSpace=nowrap;fontSize=11;fontFamily=Consolas;';
    let yy = head;
    if (hA) {
        celdas.push(`<mxCell id="${id}a" value="${xmlEsc(atr.join('<br>') || ' ')}" style="${txt}" vertex="1" parent="${id}"><mxGeometry y="${yy}" width="${w}" height="${hA}" as="geometry"/></mxCell>`);
        yy += hA;
        celdas.push(`<mxCell id="${id}l" value="" style="line;strokeWidth=1;fillColor=none;align=left;verticalAlign=middle;spacingTop=-1;spacingLeft=3;spacingRight=3;rotatable=0;labelPosition=right;points=[];portConstraint=eastwest;strokeColor=inherit;" vertex="1" parent="${id}"><mxGeometry y="${yy}" width="${w}" height="8" as="geometry"/></mxCell>`);
        yy += 8;
    }
    celdas.push(`<mxCell id="${id}m" value="${xmlEsc(met.join('<br>') || ' ')}" style="${txt}" vertex="1" parent="${id}"><mxGeometry y="${yy}" width="${w}" height="${hM}" as="geometry"/></mxCell>`);
}

const PAQ_W = 320;
function medirPaquete(p) {
    const n = Object.values(clases).filter(c => c.paquete === p && !c.nombre.includes('.')).length;
    return { w: PAQ_W, h: 44 + n * 16 };
}
function nombresPaquete(p) {
    return Object.values(clases).filter(c => c.paquete === p && !c.nombre.includes('.')).map(c => c.nombre).sort();
}

const ESTILO_DRAWIO = {
    herencia: 'endArrow=block;endFill=0;endSize=12;',
    realizacion: 'endArrow=block;endFill=0;endSize=12;dashed=1;',
    asociacion: 'endArrow=open;endFill=0;endSize=10;',
    dependencia: 'endArrow=open;endFill=0;endSize=10;dashed=1;strokeColor=#777777;',
    composicion: 'startArrow=diamondThin;startFill=1;startSize=14;endArrow=open;endFill=0;endSize=10;',
    anidada: 'startArrow=circlePlus;startFill=0;endArrow=none;',
};

function paginaDrawio(pagina) {
    const celdas = [], ids = {};
    const medir = (n, op) => pagina.paquetes ? medirPaquete(n) : medirDrawio(n, op);
    const { pos, anchoTotal, altoTotal } = ubicar(pagina, medir);
    celdas.push(`<mxCell id="t${idSeq++}" value="${xmlEsc(`<b style="font-size:18px">${pagina.titulo}</b><br><span style="font-size:12px;color:#555">${pagina.sub}</span>`)}" style="text;html=1;align=left;verticalAlign=top;whiteSpace=wrap;" vertex="1" parent="1"><mxGeometry x="${X0}" y="20" width="${Math.max(900, anchoTotal - X0)}" height="60" as="geometry"/></mxCell>`);
    for (const [, , nombres, op = {}] of pagina.celdas) {
        for (const n of (Array.isArray(nombres) ? nombres : [nombres])) {
            const id = 'c' + (idSeq++); ids[n] = id;
            if (pagina.paquetes) {
                const [fill, stroke] = COLORES[n];
                const lista = nombresPaquete(n).map(x => clases[x].esInterfaz ? `<i>${x}</i>` : x).join('<br>');
                const p = pos[n];
                celdas.push(`<mxCell id="${id}" value="${xmlEsc(`<b>${n}</b><br><font style="font-size:11px">${lista}</font>`)}" style="shape=folder;fontStyle=0;spacingTop=10;tabWidth=90;tabHeight=14;tabPosition=left;html=1;whiteSpace=wrap;align=left;verticalAlign=top;spacingLeft=10;fillColor=${fill};strokeColor=${stroke};" vertex="1" parent="1"><mxGeometry x="${p.x}" y="${p.y}" width="${p.w}" height="${p.h}" as="geometry"/></mxCell>`);
            } else claseDrawio(celdas, id, n, pos[n], op);
        }
    }
    for (const r of puertos(pagina, pos)) {
        const [ex, ey] = punto(r.lados[0], r.frac[0]), [nx, ny] = punto(r.lados[1], r.frac[1]);
        let extra = '', etiqueta = '';
        if (r.de === r.a) {
            const p = pos[r.de];
            extra = `<Array as="points"><mxPoint x="${p.x + p.w + 35}" y="${p.y + p.h * 0.3}"/><mxPoint x="${p.x + p.w + 35}" y="${p.y + p.h * 0.7}"/></Array>`;
            etiqueta = clases[r.de].atributos.filter(x => x.tipo === r.de).map(x => x.nombre).join(', ') + ' 0..2';
        }
        celdas.push(`<mxCell id="e${idSeq++}" value="${xmlEsc(etiqueta)}" style="edgeStyle=orthogonalEdgeStyle;rounded=1;html=1;fontSize=10;exitX=${ex};exitY=${ey};exitDx=0;exitDy=0;entryX=${nx};entryY=${ny};entryDx=0;entryDy=0;${ESTILO_DRAWIO[r.tipo]}" edge="1" parent="1" source="${ids[r.de]}" target="${ids[r.a]}"><mxGeometry relative="1" as="geometry">${extra}</mxGeometry></mxCell>`);
    }
    // nota y leyenda
    celdas.push(`<mxCell id="n${idSeq++}" value="${xmlEsc(pagina.nota)}" style="shape=note;whiteSpace=wrap;html=1;size=14;align=left;spacingLeft=8;fontSize=11;fillColor=#fffde7;strokeColor=#b0a060;" vertex="1" parent="1"><mxGeometry x="${X0}" y="${altoTotal + 60}" width="${Math.min(900, Math.max(520, anchoTotal - X0))}" height="50" as="geometry"/></mxCell>`);
    if (!pagina.paquetes) {
        const lx = anchoTotal + 80, ly = Y0;
        celdas.push(`<mxCell id="lg${idSeq++}" value="Leyenda" style="rounded=0;whiteSpace=wrap;html=1;verticalAlign=top;fontStyle=1;fillColor=#ffffff;" vertex="1" parent="1"><mxGeometry x="${lx}" y="${ly}" width="260" height="${34 + LEYENDA.length * 26}" as="geometry"/></mxCell>`);
        LEYENDA.forEach(([t, txt], i) => {
            const yy = ly + 38 + i * 26;
            celdas.push(`<mxCell id="lg${idSeq++}" value="" style="html=1;${ESTILO_DRAWIO[t]}" edge="1" parent="1"><mxGeometry relative="1" as="geometry"><mxPoint x="${lx + 14}" y="${yy}" as="sourcePoint"/><mxPoint x="${lx + 74}" y="${yy}" as="targetPoint"/></mxGeometry></mxCell>`);
            celdas.push(`<mxCell id="lg${idSeq++}" value="${txt}" style="text;html=1;align=left;verticalAlign=middle;fontSize=11;" vertex="1" parent="1"><mxGeometry x="${lx + 84}" y="${yy - 10}" width="170" height="20" as="geometry"/></mxCell>`);
        });
    }
    return celdas;
}

const paginasXml = PAGINAS.map((p, i) =>
    `  <diagram id="pag${i + 1}" name="${xmlEsc(p.nombre)}">\n    <mxGraphModel dx="1600" dy="900" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="0" pageScale="1" math="0" shadow="0">\n      <root>\n        <mxCell id="0"/>\n        <mxCell id="1" parent="0"/>\n        ${paginaDrawio(p).join('\n        ')}\n      </root>\n    </mxGraphModel>\n  </diagram>`);
fs.mkdirSync(path.dirname(SALIDA), { recursive: true });
fs.writeFileSync(SALIDA, `<mxfile host="app.diagrams.net" type="device">\n${paginasXml.join('\n')}\n</mxfile>\n`);
console.log('draw.io:', SALIDA, '-', PAGINAS.length, 'paginas');

// ---------- Salida Lucidchart (Standard Import), opcional ----------
// LUCID_OUT=archivo.json node docs/herramientas/generar-diagrama-clases.js .
if (process.env.LUCID_OUT) {
    // Lucid interpreta < > como HTML en las clases UML: se usan comillas angulares simples.
    const plano = s => s.replace(/<[^>]+>/g, '').replace(/&lt;/g, '‹').replace(/&gt;/g, '›').replace(/&amp;/g, '&');
    const estaticos = c => new Set([...c.atributos.filter(a => a.estatico), ...c.metodos.filter(m => m.estatico)].map(x => x.nombre));
    const conStatic = (c, lineas) => lineas.map(l => { const m = l.match(/^[+\-#~] (\w+)/); return m && estaticos(c).has(m[1]) ? l + ' {static}' : l; });
    const ESTILO_LUCID = {
        herencia: ['none', 'generalization', 'solid'], realizacion: ['none', 'generalization', 'dashed'],
        asociacion: ['none', 'openArrow', 'solid'], dependencia: ['none', 'openArrow', 'dashed'],
        composicion: ['composition', 'openArrow', 'solid'], anidada: ['nesting', 'none', 'solid'],
    };
    const textoUml = (nombre) => {
        const c = clases[nombre];
        const props = conStatic(c, lineasAtributos(c).map(plano));
        const mets = conStatic(c, lineasMetodos(c).map(plano));
        const simple = nombre.split('.').pop();
        const title = c.esInterfaz ? `«interface» ${simple}` : (nombre.includes('.') ? `${simple} (interna de ${nombre.split('.')[0]})` : simple);
        return { props, mets, title };
    };
    const medirLucid = (nombre, op) => {
        const c = clases[nombre];
        if (pagLucid.paquetes) return { w: 420, h: 80 + nombresPaquete(nombre).length * 20 };
        if (!c || op.compacta) return { w: 250, h: 80 };
        const { props, mets, title } = textoUml(nombre);
        const largo = Math.max(title.length + 4, ...props.map(s => s.length), ...mets.map(s => s.length));
        const np = props.length, nm = Math.max(1, mets.length);
        return { w: Math.max(250, Math.round(largo * 7.4 + 40)), h: 56 + 21 * (np + nm) + (np ? 30 : 15) };
    };
    let pagLucid = null, n = 0;
    const nid = p => p + (n++);
    const pages = PAGINAS.map((pagina, ip) => {
        pagLucid = pagina;
        const shapes = [], lines = [], ids = {};
        const { pos, anchoTotal, altoTotal } = ubicar(pagina, medirLucid);
        shapes.push({ id: nid('t'), type: 'text', boundingBox: { x: X0, y: 10, w: Math.max(1500, anchoTotal - X0), h: 80 },
            text: `<p style="font-size:14px;text-align:left"><span style="font-size:22px"><b>${esc(pagina.titulo)}</b></span><br>${esc(pagina.sub)}</p>` });
        for (const [, , nombres, op = {}] of pagina.celdas) {
            for (const nom of (Array.isArray(nombres) ? nombres : [nombres])) {
                const p = pos[nom], sid = nid('s'); ids[nom] = sid;
                const bb = { x: p.x, y: p.y, w: p.w, h: p.h };
                const c = clases[nom];
                if (pagina.paquetes) {
                    const lista = nombresPaquete(nom).map(x => clases[x].esInterfaz ? `<i>${x}</i>` : x).join('<br>');
                    shapes.push({ id: sid, type: 'rectangle', boundingBox: bb,
                        style: { fill: { type: 'color', color: COLORES[nom][0] }, stroke: { color: COLORES[nom][1], width: 2, style: 'solid' } },
                        text: `<p style="font-size:12px;text-align:left"><span style="font-size:15px"><b>paquete ${nom}</b></span><br>${lista}</p>` });
                } else if (!c || op.compacta) {
                    const ester = op.estereotipo || (c && c.esInterfaz ? '«interface»' : '');
                    const [fill, stroke] = c ? COLORES[c.paquete] : COLORES.externo;
                    const pie = op.nota || (c ? '(detalle en pagina ' + (c.paquete === 'estructuras' ? 2 : c.paquete === 'servicio' ? 5 : 3) + ')' : '');
                    shapes.push({ id: sid, type: 'rectangle', boundingBox: bb,
                        style: { fill: { type: 'color', color: fill }, stroke: { color: stroke, width: 1, style: c ? 'solid' : 'dashed' } },
                        text: `<p style="font-size:11px;text-align:center">${ester ? ester + '<br>' : ''}<b>${nom}</b><br>${pie}</p>` });
                } else {
                    const { props, mets, title } = textoUml(nom);
                    const np = props.length, nm = Math.max(1, mets.length);
                    shapes.push({ id: sid, type: 'umlClass', boundingBox: bb, title, properties: props, methods: mets.length ? mets : [' '],
                        propertiesHeight: np ? Math.max(0.05, Math.min(0.95, (21 * np + 15) / (21 * (np + nm) + 30))) : 0.05,
                        style: { fill: { type: 'color', color: COLORES[c.paquete][0] }, stroke: { color: COLORES[c.paquete][1], width: 1, style: 'solid' } } });
                }
            }
        }
        for (const r of puertos(pagina, pos)) {
            const [e1, e2, st] = ESTILO_LUCID[r.tipo];
            const [ex, ey] = punto(r.lados[0], r.frac[0]), [nx, ny] = punto(r.lados[1], r.frac[1]);
            const linea = { id: nid('l'), lineType: 'elbow',
                endpoint1: { type: 'shapeEndpoint', style: e1, shapeId: ids[r.de], position: { x: ex, y: ey } },
                endpoint2: { type: 'shapeEndpoint', style: e2, shapeId: ids[r.a], position: { x: nx, y: ny } },
                stroke: { color: r.tipo === 'dependencia' ? '#777777' : '#333333', width: 1, style: st } };
            if (r.de === r.a) linea.text = [{ text: clases[r.de].atributos.filter(x => x.tipo === r.de).map(x => x.nombre).join(', ') + ' 0..2', position: 0.5, side: 'top' }];
            lines.push(linea);
        }
        shapes.push({ id: nid('n'), type: 'note', boundingBox: { x: X0, y: altoTotal + 80, w: Math.min(1200, Math.max(700, anchoTotal - X0)), h: 70 },
            style: { fill: { type: 'color', color: '#FFFDE7' }, stroke: { color: '#B0A060', width: 1, style: 'solid' } },
            text: `<p style="font-size:12px;text-align:left">${esc(pagina.nota)}</p>` });
        if (!pagina.paquetes) {
            // leyenda a la derecha: titulo arriba y cada ejemplo en su propia fila
            const lx = anchoTotal + 100, ly = Y0;
            shapes.push({ id: nid('lg'), type: 'text', boundingBox: { x: lx, y: ly, w: 330, h: 30 }, text: '<p style="font-size:14px;text-align:left"><b>Leyenda</b></p>' });
            LEYENDA.forEach(([t, txt], i) => {
                const yy = ly + 70 + i * 50, [e1, e2, st] = ESTILO_LUCID[t];
                lines.push({ id: nid('lgl'), lineType: 'straight',
                    endpoint1: { type: 'positionEndpoint', style: e1, position: { x: lx, y: yy } },
                    endpoint2: { type: 'positionEndpoint', style: e2, position: { x: lx + 90, y: yy } },
                    stroke: { color: '#333333', width: 1, style: st } });
                shapes.push({ id: nid('lgt'), type: 'text', boundingBox: { x: lx + 110, y: yy - 15, w: 220, h: 30 }, text: `<p style="font-size:12px;text-align:left">${txt}</p>` });
            });
        }
        return { id: 'pag' + (ip + 1), title: pagina.nombre, shapes, lines };
    });
    fs.writeFileSync(process.env.LUCID_OUT, JSON.stringify({ version: 1, pages }));
    console.log('lucid:', process.env.LUCID_OUT, pages.map(p => `${p.title}: ${p.shapes.length} formas / ${p.lines.length} lineas`).join(' | '));
}

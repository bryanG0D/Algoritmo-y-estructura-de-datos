// Genera docs/entregables/diagrama-clases.drawio leyendo el codigo Java.
// Uso (desde la raiz del proyecto):  node docs/herramientas/generar-diagrama-clases.js .
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

const PX = 6.6, LH = 17, HEAD = 30;
function anchoTexto(lineas) {
    return Math.max(...lineas.map(l => l.replace(/<[^>]+>/g, '').replace(/&\w+;/g, 'x').length)) * PX + 16;
}

// ---------- Paginas ----------
let idSeq = 2;
function nuevaPagina(nombre) { return { nombre, celdas: [], ids: {}, geo: {} }; }

function agregarClase(pag, nombre, x, y, opciones = {}) {
    const c = clases[nombre];
    const id = 'c' + (idSeq++);
    pag.ids[nombre] = id;
    if (!c || opciones.compacta) {
        // clase externa (javax.servlet...) o de otra pagina: solo el nombre
        const [fill, stroke] = c ? COLORES[c.paquete] : COLORES.externo;
        const ester = opciones.estereotipo || (c ? (c.esInterfaz ? '«interface»' : '') : '');
        const titulo = (ester ? ester + '<br>' : '') + (c && c.esInterfaz ? `<i>${nombre.split('.').pop()}</i>` : `<b>${nombre}</b>`)
            + (opciones.nota ? `<br><font style="font-size:10px">${opciones.nota}</font>` : '');
        const w = Math.max(130, anchoTexto([nombre, opciones.nota || '']) + 10);
        const h = 26 + (ester ? 14 : 0) + (opciones.nota ? 14 : 0);
        pag.celdas.push(`<mxCell id="${id}" value="${xmlEsc(titulo)}" style="rounded=0;whiteSpace=wrap;html=1;fillColor=${fill};strokeColor=${stroke};${c ? 'opacity=70;' : 'dashed=1;'}fontSize=11;" vertex="1" parent="1"><mxGeometry x="${x}" y="${y}" width="${w}" height="${h}" as="geometry"/></mxCell>`);
        return { w, h };
    }
    const [fill, stroke] = COLORES[c.paquete];
    const atr = lineasAtributos(c);
    const met = lineasMetodos(c);
    const simple = nombre.split('.').pop();
    let titulo = c.esInterfaz ? `«interface»<br><i>${simple}</i>` : `<b>${nombre.includes('.') ? nombre : simple}</b>`;
    if (nombre.includes('.')) titulo = `<b>${simple}</b><br><font style="font-size:10px">(clase interna de ${nombre.split('.')[0]})</font>`;
    const head = HEAD + (c.esInterfaz || nombre.includes('.') ? 12 : 0);
    const w = Math.max(160, anchoTexto([...atr, ...met, simple]));
    const hA = c.esInterfaz && !atr.length ? 0 : Math.max(1, atr.length) * LH + 8;
    const hM = Math.max(1, met.length) * LH + 8;
    const h = head + hA + (hA ? 8 : 0) + hM;
    pag.celdas.push(`<mxCell id="${id}" value="${xmlEsc(titulo)}" style="swimlane;fontStyle=0;align=center;verticalAlign=top;childLayout=stackLayout;horizontal=1;startSize=${head};horizontalStack=0;resizeParent=1;resizeParentMax=0;resizeLast=0;collapsible=0;marginBottom=0;html=1;fillColor=${fill};strokeColor=${stroke};fontSize=12;" vertex="1" parent="1"><mxGeometry x="${x}" y="${y}" width="${w}" height="${h}" as="geometry"/></mxCell>`);
    const txtStyle = 'text;strokeColor=none;fillColor=none;align=left;verticalAlign=top;spacingLeft=6;spacingRight=4;overflow=hidden;rotatable=0;html=1;whiteSpace=nowrap;fontSize=11;fontFamily=Consolas;';
    let yy = head;
    if (hA) {
        pag.celdas.push(`<mxCell id="${id}a" value="${xmlEsc(atr.join('<br>') || ' ')}" style="${txtStyle}" vertex="1" parent="${id}"><mxGeometry y="${yy}" width="${w}" height="${hA}" as="geometry"/></mxCell>`);
        yy += hA;
        pag.celdas.push(`<mxCell id="${id}l" value="" style="line;strokeWidth=1;fillColor=none;align=left;verticalAlign=middle;spacingTop=-1;spacingLeft=3;spacingRight=3;rotatable=0;labelPosition=right;points=[];portConstraint=eastwest;strokeColor=inherit;" vertex="1" parent="${id}"><mxGeometry y="${yy}" width="${w}" height="8" as="geometry"/></mxCell>`);
        yy += 8;
    }
    pag.celdas.push(`<mxCell id="${id}m" value="${xmlEsc(met.join('<br>') || ' ')}" style="${txtStyle}" vertex="1" parent="${id}"><mxGeometry y="${yy}" width="${w}" height="${hM}" as="geometry"/></mxCell>`);
    return { w, h };
}

const ESTILO_REL = {
    herencia: 'endArrow=block;endFill=0;endSize=12;',
    realizacion: 'endArrow=block;endFill=0;endSize=12;dashed=1;',
    asociacion: 'endArrow=open;endFill=0;endSize=10;',
    dependencia: 'endArrow=open;endFill=0;endSize=10;dashed=1;strokeColor=#777777;',
    composicion: 'startArrow=diamondThin;startFill=1;startSize=14;endArrow=open;endFill=0;endSize=10;',
    anidada: 'startArrow=circlePlus;startFill=0;endArrow=none;',
};

function conectar(pag, filtro = () => true) {
    const vistos = new Set();
    for (const r of relaciones) {
        const de = pag.ids[r.de], a = pag.ids[r.a];
        if (!de || !a || !filtro(r)) continue;
        if (de === a) {
            // autorreferencia (NodoAVL -> izquierdo / derecho)
            const g = pag.geo[r.de];
            const campos = clases[r.de].atributos.filter(x => x.tipo === r.de).map(x => x.nombre).join(', ');
            pag.celdas.push(`<mxCell id="e${idSeq++}" value="${xmlEsc(campos + ' 0..2')}" style="edgeStyle=orthogonalEdgeStyle;rounded=1;html=1;fontSize=10;exitX=1;exitY=0.25;entryX=1;entryY=0.75;${ESTILO_REL[r.tipo]}" edge="1" parent="1" source="${de}" target="${a}"><mxGeometry relative="1" as="geometry"><Array as="points"><mxPoint x="${g.x + g.w + 40}" y="${g.y + g.h * 0.25}"/><mxPoint x="${g.x + g.w + 40}" y="${g.y + g.h * 0.75}"/></Array></mxGeometry></mxCell>`);
            continue;
        }
        const k = r.de + '>' + r.a;
        if (vistos.has(k)) continue;
        vistos.add(k);
        pag.celdas.push(`<mxCell id="e${idSeq++}" value="" style="edgeStyle=orthogonalEdgeStyle;rounded=1;html=1;${ESTILO_REL[r.tipo]}" edge="1" parent="1" source="${de}" target="${a}"><mxGeometry relative="1" as="geometry"/></mxCell>`);
    }
}

// Coloca filas de clases; cada fila es una lista de nombres (o [nombre, opciones]).
function distribuir(pag, filas, x0 = 40, y0 = 70, gapX = 50, gapY = 70) {
    let y = y0, maxX = 0;
    for (const fila of filas) {
        let x = fila.x0 || x0, alto = 0;
        for (const item of fila) {
            const [n, op] = Array.isArray(item) ? item : [item, {}];
            const { w, h } = agregarClase(pag, n, x, y, op);
            pag.geo[n] = { x, y, w, h };
            x += w + gapX; alto = Math.max(alto, h);
        }
        maxX = Math.max(maxX, x);
        y += alto + gapY;
    }
    return { maxX, y };
}

function titulo(pag, texto, sub) {
    pag.celdas.push(`<mxCell id="t${idSeq++}" value="${xmlEsc(`<b style="font-size:18px">${texto}</b><br><span style="font-size:12px;color:#555">${sub}</span>`)}" style="text;html=1;align=left;verticalAlign=top;" vertex="1" parent="1"><mxGeometry x="40" y="10" width="900" height="50" as="geometry"/></mxCell>`);
}

function leyenda(pag, x, y) {
    const items = [
        ['herencia', 'Herencia (extends)'], ['realizacion', 'Implementa (implements)'],
        ['asociacion', 'Asociacion (atributo)'], ['composicion', 'Composicion (crea y contiene)'],
        ['dependencia', 'Dependencia (usa)'],
    ];
    pag.celdas.push(`<mxCell id="lg${idSeq++}" value="Leyenda" style="rounded=0;whiteSpace=wrap;html=1;verticalAlign=top;fontStyle=1;fillColor=#ffffff;" vertex="1" parent="1"><mxGeometry x="${x}" y="${y}" width="250" height="${30 + items.length * 24}" as="geometry"/></mxCell>`);
    items.forEach(([t, txt], i) => {
        const yy = y + 34 + i * 24;
        pag.celdas.push(`<mxCell id="lg${idSeq++}" value="" style="html=1;${ESTILO_REL[t]}" edge="1" parent="1"><mxGeometry relative="1" as="geometry"><mxPoint x="${x + 12}" y="${yy}" as="sourcePoint"/><mxPoint x="${x + 72}" y="${yy}" as="targetPoint"/></mxGeometry></mxCell>`);
        pag.celdas.push(`<mxCell id="lg${idSeq++}" value="${txt}" style="text;html=1;align=left;verticalAlign=middle;fontSize=11;" vertex="1" parent="1"><mxGeometry x="${x + 80}" y="${yy - 10}" width="165" height="20" as="geometry"/></mxCell>`);
    });
}

const paginas = [];

// 1) Vista general por paquetes
{
    const pag = nuevaPagina('1. Vista general');
    titulo(pag, 'Diagrama de clases - Vista general por paquetes', 'com.utp.estacionamiento - cada flecha punteada indica que un paquete usa clases de otro');
    const orden = [['servlet', 'util'], ['servicio'], ['estructuras', 'dao'], ['modelo', 'conexion']];
    const pos = {}, ids = {};
    let y = 80;
    for (const fila of orden) {
        let x = 60, alto = 0;
        for (const p of fila) {
            const nombres = Object.values(clases).filter(c => c.paquete === p && !c.nombre.includes('.')).map(c => c.nombre).sort();
            const [fill, stroke] = COLORES[p];
            const w = 300, h = 34 + Math.ceil(nombres.length / 2) * 16 + 10;
            const id = 'p' + (idSeq++); ids[p] = id;
            const cuerpo = nombres.map(n => clases[n].esInterfaz ? `<i>${n}</i>` : n);
            const mitad = Math.ceil(cuerpo.length / 2);
            const tabla = `<table style="width:100%;font-size:10px"><tr><td valign="top">${cuerpo.slice(0, mitad).join('<br>')}</td><td valign="top">${cuerpo.slice(mitad).join('<br>')}</td></tr></table>`;
            pag.celdas.push(`<mxCell id="${id}" value="${xmlEsc(`<b>${p}</b>${tabla}`)}" style="shape=folder;fontStyle=0;spacingTop=10;tabWidth=80;tabHeight=14;tabPosition=left;html=1;whiteSpace=wrap;align=left;verticalAlign=top;spacingLeft=8;fillColor=${fill};strokeColor=${stroke};" vertex="1" parent="1"><mxGeometry x="${x}" y="${y}" width="${w}" height="${h}" as="geometry"/></mxCell>`);
            x += w + 120; alto = Math.max(alto, h);
        }
        y += alto + 80;
    }
    const deps = new Set();
    for (const r of relaciones) {
        const a = clases[r.de], b = clases[r.a];
        if (!a || !b || a.paquete === b.paquete) continue;
        deps.add(a.paquete + '>' + b.paquete);
    }
    for (const d of deps) {
        const [a, b] = d.split('>');
        pag.celdas.push(`<mxCell id="e${idSeq++}" value="" style="edgeStyle=orthogonalEdgeStyle;rounded=1;html=1;${ESTILO_REL.dependencia}" edge="1" parent="1" source="${ids[a]}" target="${ids[b]}"><mxGeometry relative="1" as="geometry"/></mxCell>`);
    }
    paginas.push(pag);
}

// 2) Estructuras de datos y modelo
{
    const pag = nuevaPagina('2. Estructuras y modelo');
    titulo(pag, 'Estructuras de datos y modelo', 'Arbol AVL (busqueda por placa O(log n)) y Matriz 5x10 (asignacion de espacios). El modelo refleja las 6 tablas de MySQL.');
    const dim = distribuir(pag, [
        ['ArbolAVL', 'NodoAVL', 'ResultadoBusquedaAVL', 'MatrizEstacionamiento'],
        ['Vehiculo', 'TipoVehiculo', 'EspacioEstacionamiento', 'Ticket'],
        ['Pago', 'Usuario', ['Serializable', { estereotipo: '«interface»', nota: 'java.io' }]],
    ], 40, 70, 60, 90);
    leyenda(pag, dim.maxX + 20, 70);
    conectar(pag);
    paginas.push(pag);
}

// 3) Capa de acceso a datos (DAO)
{
    const pag = nuevaPagina('3. Patron DAO');
    titulo(pag, 'Capa de acceso a datos - Patron DAO', 'Cada entidad tiene una interfaz DAO y su implementacion JDBC. TicketDAOImpl y PagoDAOImpl llaman a los procedimientos almacenados.');
    const dim = distribuir(pag, [
        ['VehiculoDAO', 'TipoVehiculoDAO', 'UsuarioDAO', 'EspacioEstacionamientoDAO'],
        ['VehiculoDAOImpl', 'TipoVehiculoDAOImpl', 'UsuarioDAOImpl', 'EspacioEstacionamientoDAOImpl'],
        ['TicketDAO', 'PagoDAO', 'ResultadoEntrada', 'ResultadoSalida', 'ReporteIngresoDia'],
        ['TicketDAOImpl', 'PagoDAOImpl', 'ConexionBD'],
    ], 40, 70, 40, 80);
    leyenda(pag, dim.maxX + 20, 70);
    conectar(pag);
    paginas.push(pag);
}

// 4) Servicio y capa web
{
    const pag = nuevaPagina('4. Servicio y web');
    titulo(pag, 'Servicio y capa web (Servlets)', 'Los Servlets obtienen el unico EstacionamientoService desde el ServletContext. Todos usan GsonProvider para responder JSON (no se dibuja para no saturar).');
    const dim = distribuir(pag, [
        Object.assign([['HttpServlet', { nota: 'javax.servlet.http' }]], { x0: 640 }),
        ['LoginServlet', 'LogoutServlet', 'TipoVehiculoServlet', 'VehiculoServlet', 'ReporteServlet'],
        ['EntradaServlet', 'SalidaServlet', 'BusquedaServlet', 'MapaServlet', 'ResumenServlet'],
        ['EstacionamientoService', 'EstacionamientoService.ResultadoRegistro', 'EstacionamientoService.ResultadoCobro', 'CompatibilidadEspacio'],
        [['ArbolAVL', { compacta: true }], ['UsuarioDAO', { compacta: true }], ['MatrizEstacionamiento', { compacta: true }], ['VehiculoDAO', { compacta: true }], ['TicketDAO', { compacta: true }], ['PagoDAO', { compacta: true }], ['EspacioEstacionamientoDAO', { compacta: true }], ['TipoVehiculoDAO', { compacta: true }]],
        ['AppContextListener', 'FiltroSesion', 'GsonProvider', ['ServletContextListener', { estereotipo: '«interface»', nota: 'javax.servlet' }], ['Filter', { estereotipo: '«interface»', nota: 'javax.servlet' }]],
    ], 40, 70, 40, 90);
    leyenda(pag, dim.maxX + 20, 70);
    conectar(pag, r => !(r.a === 'GsonProvider' && r.tipo === 'dependencia')
        && !(clases[r.de] && clases[r.de].paquete === 'servlet' && r.a !== 'EstacionamientoService' && r.a !== 'HttpServlet' && r.tipo !== 'asociacion' && !r.de.includes('.')));
    paginas.push(pag);
}

// ---------- Escritura ----------
const xml = `<mxfile host="app.diagrams.net" type="device">\n` + paginas.map((p, i) =>
    `  <diagram id="pag${i + 1}" name="${xmlEsc(p.nombre)}">\n    <mxGraphModel dx="1600" dy="900" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="0" pageScale="1" math="0" shadow="0">\n      <root>\n        <mxCell id="0"/>\n        <mxCell id="1" parent="0"/>\n        ${p.celdas.join('\n        ')}\n      </root>\n    </mxGraphModel>\n  </diagram>`).join('\n') + `\n</mxfile>\n`;
fs.mkdirSync(path.dirname(SALIDA), { recursive: true });
fs.writeFileSync(SALIDA, xml);

// Resumen para revisar
for (const c of Object.values(clases))
    console.log(`${c.paquete}/${c.nombre}${c.esInterfaz ? ' [I]' : ''}: ${c.atributos.length} atr, ${c.metodos.length} met${c.extiende ? ' ext ' + c.extiende : ''}${c.implementa.length ? ' impl ' + c.implementa : ''}`);
console.log('relaciones:', relaciones.length, '->', SALIDA);

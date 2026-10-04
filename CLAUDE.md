# Contexto del proyecto para Claude Code

Proyecto final grupal del curso **Algoritmos y Estructuras de Datos** (UTP):
sistema de estacionamiento "inteligente". Aplicacion web en Java 17 con Maven
(packaging `war`), Servlets + JSP + JavaScript, MySQL y patron DAO.
Se trabaja **en local** con Apache NetBeans (requisito del docente). El codigo
se comparte con el grupo en un repositorio de GitHub (ver `.gitignore`: nunca
se suben `db.properties`, `target/` ni respaldos `.sql`).

Material de referencia dentro de esta carpeta:
- `docs/consigna/PROY_FINAL.pdf`: consigna oficial del curso (solo local, no se sube a GitHub).
- `docs/diagramas/diagrama-ER-aprobado.png`: diagrama E-R aprobado por el docente.
- `docs/entregables/`: donde van UML, documentacion y diapositivas.
- `LEEME-PRIMERO.md`: guia para el equipo (estado y pasos).

Responde siempre en espanol, breve y directo. Antes de cambios grandes,
presenta un plan con la lista de archivos que vas a tocar y espera el OK.

## Decisiones vigentes (no revertir)

- **Esquema aprobado por el docente: 6 tablas.** `tipo_vehiculo`, `usuario`,
  `vehiculo`, `espacio_estacionamiento`, `ticket`, `pago`. No agregues,
  quites ni modifiques tablas o columnas: cualquier cambio de esquema necesita
  nueva aprobacion del docente. Los procedimientos almacenados si se pueden
  modificar.
- La tarifa vive en `tipo_vehiculo` (`precio_hora`, `precio_fraccion`,
  `vigente_desde`). `espacio_estacionamiento.id_zona` es un INT descriptivo
  sin FK. `ticket.id_espacio` es NOT NULL: un ticket solo existe si el
  vehiculo ya tiene espacio.
- **Sin lista de espera.** El docente dijo: "si no hay espacio en el
  estacionamiento por lo general yo no espero y voy a buscar otro sitio,
  entonces les diria que no se compliquen". Si no hay espacio compatible, el
  sistema lo informa y no crea ticket. No reintroduzcas ninguna cola.
- Credenciales de MySQL en `src/main/resources/db.properties` (plantilla:
  `db.properties.example`). Nunca escribas credenciales en el codigo ni
  muestres la contrasena en tus respuestas.

## Estructuras de datos (lo que el docente evalua)

- **ArbolAVL** (`estructuras/ArbolAVL.java`): estructura eje del proyecto.
  Indexa vehiculos por placa; busqueda O(log n) cuando un cliente pierde su
  ticket. Se carga desde MySQL al arrancar.
- **MatrizEstacionamiento** (`estructuras/MatrizEstacionamiento.java`): mapa
  fisico de 5 filas x 10 columnas = 50 espacios. Columnas 1 a 9: espacios de
  auto (`id_tipo_permitido = 1`). Columna 10: espacios de moto
  (`id_tipo_permitido = 2`). Es la que decide que espacio se asigna.

## Cambio ya implementado (version actual)

Ya esta hecho y probado contra una base real. NO lo vuelvas a implementar:

- Se eliminaron la cola de espera (`ColaFIFO`, `SolicitudEspera`), la
  reasignacion automatica y `sp_asignar_espacio_a_vehiculo`.
- **3 tipos de vehiculo** (el docente acepta menos de 20 filas si los datos
  tienen sentido): 1 Auto / Camioneta (S/ 4), 2 Moto (S/ 2), 3 Bicicleta
  (S/ 1). Se cobra segun el espacio que ocupa; no se distingue SUV de 4x4.
  Buses, combis y trailers no se admiten (no caben en un espacio).
- `servicio/CompatibilidadEspacio`: regla unica. Tipos 2 y 3 (moto,
  bicicleta) van a espacios con `id_tipo_permitido = 2`; el tipo 1 a
  `id_tipo_permitido = 1`.
- `MatrizEstacionamiento.buscarEspacioEnFilaMasLibre`: elige la fila con mas
  espacios libres del tipo pedido (empate: menor fila) y devuelve su primer
  espacio libre.
- `sp_registrar_entrada(placa, marca, modelo, color, id_tipo, id_usuario,
  id_espacio, OUT id_ticket)`: recibe el espacio elegido por Java, busca o
  crea el vehiculo, ocupa el espacio solo si sigue LIBRE y crea el ticket.
  Si no estaba libre devuelve NULL; el servicio lo marca ocupado y reintenta
  una vez.
- Si no hay espacio: "Estacionamiento lleno..." y no se crea ticket.
- Un solo script: `database/estacionamiento_inteligente.sql` crea la base
  completa (6 tablas, 3 tipos, 4 procedimientos). Los scripts de migracion se
  eliminaron porque nadie del grupo tiene una base anterior.

### Primera tarea en la PC del equipo: verificar

1. Respaldo de la base (ver reglas abajo).
2. Ejecutar `estacionamiento_inteligente.sql` (borra y recrea la base).
3. `mvn compile`, luego `mvn jetty:run`; en el log debe aparecer
   "EstacionamientoService inicializado correctamente".
4. Probar en el navegador: login, entrada de un auto (tipo 1), una moto
   (tipo 2) y una bicicleta (tipo 3), busqueda por placa, salida con cobro,
   mapa y reporte.
5. Informar cualquier error antes de corregirlo.

## Reglas para tocar la base de datos local

1. Antes del primer cambio, haz un respaldo:
   `mysqldump -u <usuario> -p estacionamiento_inteligente > respaldo.sql`
2. Muestra el SQL que vas a ejecutar y espera confirmacion.
3. **Nunca** ejecutes el script completo sin permiso: empieza con
   `DROP DATABASE` y borra todos los datos.
4. Si `mysql` no esta en el PATH, buscalo en
   `C:\Program Files\MySQL\MySQL Server 8.0\bin\` o `C:\xampp\mysql\bin\`.
5. Si `mvn` no esta en el PATH, NetBeans trae Maven dentro de su carpeta de
   instalacion (subcarpeta `java\maven\bin`).

## Arquitectura

```
modelo/       POJOs de las 6 entidades
estructuras/  ArbolAVL, NodoAVL, MatrizEstacionamiento (sin cola de espera)
conexion/     ConexionBD (lee db.properties)
dao/          Interfaz + Impl por entidad; TicketDAOImpl y PagoDAOImpl llaman a los procedimientos
servicio/     EstacionamientoService (integra AVL + Matriz + DAO), CompatibilidadEspacio
servlet/      Login, Logout, Entrada, Salida, Busqueda, Mapa, Vehiculo (CRUD), TipoVehiculo, Reporte
util/         AppContextListener (inicializa el servicio), GsonProvider
webapp/       JSP, css, js. Servlets registrados en WEB-INF/web.xml (sin anotaciones)
database/     estacionamiento_inteligente.sql (unico script: crea todo)
```

## Trampas conocidas

- **Gson y java.time:** en JDK 17+ Gson no puede reflejar `LocalDate` /
  `LocalDateTime`. Todo Servlet usa `GsonProvider.gson()`, nunca `new Gson()`.
- **Tomcat 9, no 10+:** el proyecto usa `javax.servlet`.
- **Login:** compara la contrasena en texto plano con `contrasena_hash`
  (simplificacion academica). Para entrar:
  `UPDATE usuario SET contrasena_hash='admin123' WHERE usuario_login='carlos.ram1';`
- **No validado aun en una PC real:** `mvn jetty:run` con dependencias de
  Maven Central y el `nbactions.xml` (F6 en NetBeans). Las pruebas se hicieron
  en un entorno aislado con MariaDB y su driver JDBC.

## Tareas pendientes (despues de verificar)

1. Probar en NetBeans: F6 deberia levantar Jetty via `nbactions.xml`; si pide
   servidor, usar Tomcat 9.
2. Diagrama de clases UML (entregable 3) como `.drawio` en `docs/entregables/`, reflejando
   la estructura ya sin cola. Herramientas permitidas por la consigna:
   StarUML, Rational, Draw.io, Lucidchart.
3. Documentacion (entregable 1) en `docs/entregables/documentacion.md`: empresa,
   problematica, objetivos, alcance, requerimientos funcionales.
4. Diapositivas y PDF de la exposicion (`ProyectoG##.pptx` / `.pdf`).
   Exposicion: lunes 07/12/2026, 25 min, 2 expositores al azar. El docente
   pregunta a **todos** los integrantes sobre la logica y el codigo, con nota
   individual: cuando el equipo pida explicaciones, prioriza que entiendan.
5. Entrega final en UTP+CLASS: `ProyectoG##.rar`.

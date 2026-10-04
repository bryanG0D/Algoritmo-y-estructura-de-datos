# LEEME PRIMERO

## Que estamos haciendo

Un **sistema de estacionamiento inteligente**: una pagina web hecha en Java
(con base de datos MySQL) donde el guardia registra los autos que entran y
salen, cobra, busca un auto por su placa y ve el mapa del estacionamiento.

Lo "inteligente" son dos estructuras de datos del curso:

- **Arbol AVL**: guarda los vehiculos ordenados por placa y encuentra
  cualquiera muy rapido (por ejemplo, si un cliente pierde su ticket).
- **Matriz 5x10**: es el mapa del estacionamiento (50 espacios). Cuando
  entra un auto, elige el espacio de la fila con mas lugares libres.

Si no hay espacio, el sistema avisa "estacionamiento lleno" (sin lista de
espera, como indico el profesor).

## Que hay en esta carpeta

```
ProyectoEstacionamiento/
|-- LEEME-PRIMERO.md      <- este archivo
|-- CLAUDE.md             <- contexto para Claude Code (no hace falta leerlo)
|-- README.md             <- detalles tecnicos e instrucciones completas
|-- pom.xml, nbactions.xml <- configuracion del proyecto (no tocar)
|-- database/
|   |-- estacionamiento_inteligente.sql   <- crea la base completa
|-- src/                  <- todo el codigo Java, las paginas y la configuracion
|-- docs/
    |-- consigna/         <- la consigna del profesor (PDF, solo local: pidela al grupo o descargala de UTP+CLASS)
    |-- diagramas/        <- el diagrama E-R aprobado
    |-- entregables/      <- aqui van UML, documentacion y diapositivas
```

## Estado segun la consigna

| # | Entregable | Estado |
|---|---|---|
| 1 | Documentacion (empresa, problematica, objetivos, alcance, requerimientos) | Pendiente |
| 2 | Diagrama E-R | Hecho y aprobado (`docs/diagramas/`) |
| 3 | Diagrama de clases UML | Pendiente |
| 4 | Modelo relacional MySQL | Hecho en el script. Para la imagen: MySQL Workbench > Database > Reverse Engineer |
| 5 | Script SQL con 20+ filas por tabla y procedimientos | Hecho (`database/`). `tipo_vehiculo` tiene solo 3 filas a proposito: Auto / Camioneta, Moto y Bicicleta (se cobra segun el espacio; el profesor acepta menos filas si los datos tienen sentido) |
| 6 | CRUD | Hecho (pantalla Vehiculos) |
| 7 | Patron DAO | Hecho (`src/.../dao/`) |
| 8 | Aplicacion web JSP + Servlets | Hecho, falta probarla en tu PC |
| 9 | Reportes parametrizados | Hecho (reporte de ingresos por fechas) |

Exposicion: **lunes 07/12/2026**, 25 minutos, 2 expositores al azar. El
profesor pregunta a todos sobre el codigo, asi que todos deben entenderlo.

## Pasos para hacerlo funcionar (en orden)

1. **Base de datos.** En MySQL Workbench: `File > Open SQL Script` >
   `database/estacionamiento_inteligente.sql` > boton del rayo.
2. **Tu contrasena de MySQL.** En PowerShell, dentro de esta carpeta:
   ```
   Copy-Item src\main\resources\db.properties.example src\main\resources\db.properties
   notepad src\main\resources\db.properties
   ```
   Cambia `db.usuario` y `db.contrasena` por los tuyos. Guarda.
3. **Usuario para entrar a la app.** En Workbench ejecuta:
   ```
   UPDATE estacionamiento_inteligente.usuario
   SET contrasena_hash = 'admin123' WHERE usuario_login = 'carlos.ram1';
   ```
4. **Abrir en NetBeans:** `File > Open Project` > esta carpeta.
5. **Ejecutar:** F6. Luego abre `http://localhost:8080/login.jsp` y entra con
   `carlos.ram1` / `admin123`.

Si algo falla en cualquier paso, copia el error completo y pidele ayuda a
Claude Code (abierto en esta carpeta, modo Local).

## Para compartir con el grupo

El proyecto esta en GitHub. Cada uno lo clona (GitHub Desktop: `File > Clone
repository`, o `git clone <url>`), sigue los pasos de arriba y crea su propio
`db.properties`. Ese archivo, la carpeta `target/` y los respaldos `.sql` no
se suben: el `.gitignore` los excluye.

# Sistema de Estacionamiento Inteligente

Proyecto final del curso Algoritmos y Estructuras de Datos (UTP). Aplicacion
web (Java Servlets + JSP + JavaScript) sobre MySQL, con patron DAO y tres
estructuras de datos en memoria: **Arbol AVL** (busqueda de placas) y
**Matriz 2D** (mapa del estacionamiento, que decide donde estacionar).

## 1. Requisitos previos

- JDK 17 o superior
- MySQL corriendo localmente
- Apache NetBeans (version 17 o superior)
- Maven (NetBeans ya lo trae integrado, no necesitas instalarlo aparte)

## 2. Base de datos

1. Ejecuta el script `database/estacionamiento_inteligente.sql` en tu MySQL
   (Workbench, phpMyAdmin o consola). Crea la base de datos, las 6 tablas,
   los datos iniciales y los 4 procedimientos almacenados.
2. Configura tus credenciales **sin tocar el codigo**:
   - Ve a `src/main/resources/`
   - Copia `db.properties.example` y nombra la copia `db.properties`
   - Edita `db.properties` con tu usuario y contrasena de MySQL

   `db.properties` es solo tuyo: no se sube a GitHub (esta en el `.gitignore`), asi tu contrasena
   no pisa la de tus companeros. Si no creas el archivo, el sistema
   usa `root` sin contrasena (lo tipico en XAMPP). En la consola veras cual
   de los dos casos se aplico al arrancar.

## 3. Abrir el proyecto en Apache NetBeans

1. `File > Open Project...` y selecciona la carpeta del proyecto (la que
   contiene el `pom.xml`). NetBeans reconoce los proyectos Maven de forma
   nativa: veras el icono de Maven sobre la carpeta.
2. La primera vez que lo abres, NetBeans descarga las dependencias
   (conector MySQL, Servlet API, Gson). Requiere conexion a internet.
3. Si aparece un aviso de "resolve project problems", dale a **Resolve** y
   deja que termine de descargar las dependencias.

> **Nota sobre el tipo de proyecto:** la consigna menciona "Java
> Application", que en NetBeans es el tipo de proyecto de escritorio. Este
> proyecto es una aplicacion **web** (Maven / WAR), que es la otra opcion
> que la consigna permite explicitamente en el punto 8 (JSP con Servlets).
> Si tu profesor exige el tipo de proyecto literal, habria que convertir la
> interfaz a formularios Swing; las capas de modelo, estructuras, DAO y
> servicio se reutilizarian sin cambios.

## 4. Ejecutar la aplicacion

**Opcion A - Jetty (mas simple, no requiere instalar servidor):**

El proyecto incluye un archivo `nbactions.xml` que hace que el boton
**Run Project (F6)** de NetBeans levante el servidor Jetty directamente.
La aplicacion queda en `http://localhost:8080/`.

Si prefieres hacerlo manualmente, tambien puedes hacer clic derecho sobre
el proyecto > `Custom > Goals...` y escribir `jetty:run`.

**Opcion B - Tomcat (si el profesor pide ver el despliegue en un servidor):**

1. Descarga **Tomcat 9.x** y descomprimelo en una carpeta.

   > **Importante:** tiene que ser Tomcat **9**, no 10 ni superior. A partir
   > de Tomcat 10 el paquete `javax.servlet` cambio de nombre a
   > `jakarta.servlet`, y este proyecto (como pide la consigna) usa
   > `javax.servlet`. Con Tomcat 10+ los Servlets simplemente no cargan.

2. En NetBeans: `Tools > Servers > Add Server...`, elige **Apache Tomcat or
   TomEE**, y apunta a la carpeta donde lo descomprimiste. Define un usuario
   y contrasena para el manager cuando te lo pida.
3. Borra el archivo `nbactions.xml` del proyecto (para que NetBeans vuelva a
   su comportamiento normal de desplegar en el servidor).
4. Clic derecho en el proyecto > `Properties > Run` y selecciona el Tomcat
   que acabas de registrar. Ahora F6 despliega ahi.

Tambien puedes generar el `.war` con clic derecho > `Build` (queda en
`target/estacionamiento.war`) y desplegarlo a mano.

## Trabajo en grupo

El proyecto se comparte en un repositorio de GitHub. Cada integrante lo
clona, crea su propio `db.properties` (seccion 2) y ejecuta el script SQL en
su MySQL. Antes de trabajar hagan `git pull` y no editen dos personas el
mismo archivo a la vez.

El `.gitignore` evita subir `db.properties` (contrasena), `target/`
(compilados) y respaldos `.sql` de la raiz.

## 5. Iniciar sesion

Los datos de ejemplo del script SQL traen usuarios con "hashes" de
contrasena ficticios (son datos de prueba, no contrasenas reales). Para
poder iniciar sesion, actualiza manualmente la contrasena de un usuario a
un valor simple:

```sql
UPDATE usuario SET contrasena_hash = 'admin123' WHERE usuario_login = 'carlos.ram1';
```

Luego entra con usuario `carlos.ram1` y contrasena `admin123` (o el login
que hayas elegido). Este login compara la contrasena en texto plano contra
`contrasena_hash` — es una simplificacion academica; en un entorno real se
usaria un algoritmo como BCrypt.

## 6. Que hace cada pantalla

| Pantalla | Que muestra / prueba |
|---|---|
| Registrar entrada | La **Matriz** elige el espacio: busca la fila con mas lugares libres del tipo adecuado (motos y bicicletas van a la columna 10; autos y camionetas a las columnas 1 a 9). Si no hay espacio, informa "estacionamiento lleno" y no crea ticket |
| Registrar salida | Cobra segun la tarifa del tipo de vehiculo (Auto / Camioneta S/ 4, Moto S/ 2, Bicicleta S/ 1 por hora) y libera el espacio |
| Buscar por placa | Consulta el **Arbol AVL en memoria** (O(log n)), no la base de datos directamente |
| Mapa del estacionamiento | Dibuja la **Matriz 2D** (5x10) con el estado real de cada espacio |
| Vehiculos | CRUD completo (Crear, Leer, Actualizar, Eliminar) usando el patron DAO |
| Reporte de ingresos | Llama al procedimiento almacenado parametrizado por rango de fechas |

## 7. Estructura del proyecto

```
pom.xml           - Configuracion Maven (dependencias y plugins)
nbactions.xml     - Hace que F6 en NetBeans levante Jetty (ver seccion 4)
database/         - estacionamiento_inteligente.sql (base completa desde cero)
src/main/resources/db.properties.example - Plantilla de credenciales
src/main/java/com/utp/estacionamiento/
  modelo/         - Clases POJO (Vehiculo, Ticket, Pago, etc.)
  estructuras/    - ArbolAVL, MatrizEstacionamiento
  conexion/       - ConexionBD (punto unico de conexion JDBC)
  dao/            - Interfaces + implementaciones (patron DAO)
                    TicketDAOImpl invoca los procedimientos almacenados
  servicio/       - EstacionamientoService (integra AVL + Matriz + DAO)
                    CompatibilidadEspacio (regla moto/auto)
  servlet/        - Los Servlets (controladores web)
  util/           - AppContextListener (carga todo al arrancar), GsonProvider
src/main/webapp/  - Paginas JSP, CSS y JavaScript
```

## 8. Para la sustentacion

Un buen hilo conductor para explicar el proyecto:

1. Llega un vehiculo (`EntradaServlet` -> `EstacionamientoService`). El
   `ArbolAVL` revisa en O(log n) si la placa ya es conocida.
2. `CompatibilidadEspacio` decide a que clase de espacio puede ir
   (moto o auto).
3. La `MatrizEstacionamiento` cuenta los espacios libres de cada fila y elige
   la fila mas vacia: asi guia a los autos hacia la zona con mas lugares.
4. El procedimiento `sp_registrar_entrada` confirma que el espacio siga libre,
   lo ocupa y crea el ticket.
5. Si no hay espacio compatible, el sistema lo informa y no crea ticket. No
   hay lista de espera, por indicacion del docente: un conductor no espera,
   busca otro estacionamiento.
6. Si alguien pierde su ticket, `BusquedaServlet` encuentra la placa en el
   `ArbolAVL` sin recorrer todos los vehiculos.

## Nota sobre las pruebas

Antes de entregarlo se compilo y se probo contra una base de datos real:
asignacion por la fila con mas lugares libres, compatibilidad moto/auto,
"estacionamiento lleno" sin crear ticket, salida con cobro, busqueda en el
Arbol AVL, y la actualizacion de procedimientos sin perdida de datos.
Falta probarlo en una PC con NetBeans y el conector oficial de MySQL.

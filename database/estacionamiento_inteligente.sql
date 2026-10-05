-- =========================================================
-- SISTEMA DE ESTACIONAMIENTO
-- Script de creacion de la base de datos
-- Algoritmos y Estructuras de Datos - UTP
-- =========================================================

DROP DATABASE IF EXISTS estacionamiento_inteligente;
CREATE DATABASE estacionamiento_inteligente
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_spanish2_ci;
USE estacionamiento_inteligente;
SET time_zone = '-05:00';  -- hora de Peru (los tickets activos se fechan con NOW())

-- =========================================================
-- TABLAS (orden respetando dependencias de llaves foraneas)
-- =========================================================

CREATE TABLE tipo_vehiculo (
    id_tipo          INT AUTO_INCREMENT PRIMARY KEY,
    nombre           VARCHAR(30)  NOT NULL,
    descripcion      VARCHAR(100),
    precio_hora      DECIMAL(6,2) NOT NULL,
    precio_fraccion  DECIMAL(6,2) NOT NULL,
    vigente_desde    DATE NOT NULL
) ENGINE=InnoDB;

CREATE TABLE usuario (
    id_usuario       INT AUTO_INCREMENT PRIMARY KEY,
    nombre           VARCHAR(60)  NOT NULL,
    usuario_login    VARCHAR(30)  NOT NULL UNIQUE,
    contrasena_hash  VARCHAR(255) NOT NULL,
    rol              VARCHAR(20)  NOT NULL  -- GUARDIA, ADMINISTRADOR
) ENGINE=InnoDB;

CREATE TABLE vehiculo (
    id_vehiculo  INT AUTO_INCREMENT PRIMARY KEY,
    placa        VARCHAR(10) NOT NULL UNIQUE,
    marca        VARCHAR(30),
    id_tipo      INT NOT NULL,
    CONSTRAINT fk_vehiculo_tipo FOREIGN KEY (id_tipo)
        REFERENCES tipo_vehiculo(id_tipo)
) ENGINE=InnoDB;

CREATE TABLE espacio_estacionamiento (
    id_espacio          INT AUTO_INCREMENT PRIMARY KEY,
    id_zona             INT NOT NULL,  -- zona del estacionamiento (dato descriptivo)
    numero_espacio      VARCHAR(10) NOT NULL,
    fila                INT NOT NULL,
    columna             INT NOT NULL,
    estado              VARCHAR(15) NOT NULL DEFAULT 'LIBRE', -- LIBRE, OCUPADO, MANTENIMIENTO
    id_tipo_permitido   INT NOT NULL,
    CONSTRAINT fk_espacio_tipo FOREIGN KEY (id_tipo_permitido)
        REFERENCES tipo_vehiculo(id_tipo),
    CONSTRAINT uq_espacio_fila_col UNIQUE (fila, columna)
) ENGINE=InnoDB;

CREATE TABLE ticket (
    id_ticket             INT AUTO_INCREMENT PRIMARY KEY,
    id_vehiculo           INT NOT NULL,
    id_espacio            INT NOT NULL,  -- un ticket solo existe si el vehiculo ya tiene espacio
    id_usuario_registro   INT NOT NULL,
    fecha_hora_entrada    DATETIME NOT NULL,
    fecha_hora_salida     DATETIME NULL,
    estado_ticket         VARCHAR(15) NOT NULL DEFAULT 'ACTIVO', -- ACTIVO, FINALIZADO
    monto_total           DECIMAL(8,2) NULL,
    CONSTRAINT fk_ticket_vehiculo FOREIGN KEY (id_vehiculo)
        REFERENCES vehiculo(id_vehiculo),
    CONSTRAINT fk_ticket_espacio FOREIGN KEY (id_espacio)
        REFERENCES espacio_estacionamiento(id_espacio),
    CONSTRAINT fk_ticket_usuario FOREIGN KEY (id_usuario_registro)
        REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

CREATE TABLE pago (
    id_pago             INT AUTO_INCREMENT PRIMARY KEY,
    id_ticket           INT NOT NULL,
    monto               DECIMAL(8,2) NOT NULL,
    metodo_pago         VARCHAR(20) NOT NULL, -- EFECTIVO, TARJETA, YAPE, PLIN
    fecha_hora_pago     DATETIME NOT NULL,
    id_usuario_cobro    INT NOT NULL,
    CONSTRAINT fk_pago_ticket FOREIGN KEY (id_ticket)
        REFERENCES ticket(id_ticket),
    CONSTRAINT fk_pago_usuario FOREIGN KEY (id_usuario_cobro)
        REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

-- =========================================================
-- DATOS INICIALES
-- =========================================================

INSERT INTO tipo_vehiculo (nombre, descripcion, precio_hora, precio_fraccion, vigente_desde) VALUES
('Auto / Camioneta', 'Sedan, hatchback, SUV, pickup, van o camioneta', 4.00, 1.00, '2026-01-01'),
('Moto', 'Motocicleta lineal o scooter', 2.00, 0.50, '2026-01-01'),
('Bicicleta', 'Bicicleta convencional', 1.00, 0.50, '2026-01-01');

INSERT INTO usuario (nombre, usuario_login, contrasena_hash, rol) VALUES
('Carlos Ramirez', 'carlos.ram1', '$2y$10$hashdemo001abcdefghijklmnopqrstuvwx', 'ADMINISTRADOR'),
('Maria Torres', 'maria.tor2', '$2y$10$hashdemo002abcdefghijklmnopqrstuvwx', 'GUARDIA'),
('Luis Fernandez', 'luis.fer3', '$2y$10$hashdemo003abcdefghijklmnopqrstuvwx', 'GUARDIA'),
('Ana Quispe', 'ana.qui4', '$2y$10$hashdemo004abcdefghijklmnopqrstuvwx', 'GUARDIA'),
('Jorge Salazar', 'jorge.sal5', '$2y$10$hashdemo005abcdefghijklmnopqrstuvwx', 'GUARDIA'),
('Rosa Mendoza', 'rosa.men6', '$2y$10$hashdemo006abcdefghijklmnopqrstuvwx', 'GUARDIA'),
('Pedro Vargas', 'pedro.var7', '$2y$10$hashdemo007abcdefghijklmnopqrstuvwx', 'GUARDIA'),
('Lucia Castillo', 'lucia.cas8', '$2y$10$hashdemo008abcdefghijklmnopqrstuvwx', 'ADMINISTRADOR'),
('Miguel Rojas', 'miguel.roj9', '$2y$10$hashdemo009abcdefghijklmnopqrstuvwx', 'GUARDIA'),
('Elena Paredes', 'elena.par10', '$2y$10$hashdemo010abcdefghijklmnopqrstuvwx', 'GUARDIA'),
('Diego Huaman', 'diego.hua11', '$2y$10$hashdemo011abcdefghijklmnopqrstuvwx', 'ADMINISTRADOR'),
('Karla Sanchez', 'karla.san12', '$2y$10$hashdemo012abcdefghijklmnopqrstuvwx', 'GUARDIA'),
('Victor Chavez', 'victor.cha13', '$2y$10$hashdemo013abcdefghijklmnopqrstuvwx', 'GUARDIA'),
('Sofia Delgado', 'sofia.del14', '$2y$10$hashdemo014abcdefghijklmnopqrstuvwx', 'GUARDIA'),
('Raul Nunez', 'raul.nun15', '$2y$10$hashdemo015abcdefghijklmnopqrstuvwx', 'GUARDIA'),
('Patricia Leon', 'patricia.leo16', '$2y$10$hashdemo016abcdefghijklmnopqrstuvwx', 'GUARDIA'),
('Andres Flores', 'andres.flo17', '$2y$10$hashdemo017abcdefghijklmnopqrstuvwx', 'GUARDIA'),
('Gabriela Rios', 'gabriela.rio18', '$2y$10$hashdemo018abcdefghijklmnopqrstuvwx', 'GUARDIA'),
('Fernando Cruz', 'fernando.cru19', '$2y$10$hashdemo019abcdefghijklmnopqrstuvwx', 'GUARDIA'),
('Monica Silva', 'monica.sil20', '$2y$10$hashdemo020abcdefghijklmnopqrstuvwx', 'GUARDIA');

INSERT INTO vehiculo (placa, marca, id_tipo) VALUES
('RGW-765', 'Toyota', 1),
('W8N-325', 'Toyota', 1),
('O9I-928', 'Kia', 1),
('AYZ-263', 'Hyundai', 1),
('W6K-384', 'Nissan', 1),
('E3Y-444', 'Suzuki', 1),
('DCM-199', 'Chevrolet', 1),
('L5T-370', 'Honda', 1),
('Z0X-570', 'Kia', 1),
('RDM-180', 'Hyundai', 1),
('R4U-733', 'Toyota', 1),
('L9G-821', 'Nissan', 1),
('CBV-333', 'Yamaha', 2),
('Y4C-975', 'Honda', 2),
('H1M-384', 'Bajaj', 2),
('OUL-266', 'Volkswagen', 1),
('L5G-786', 'Mitsubishi', 1),
('I1T-750', 'Toyota', 1),
('FRX-350', 'Kia', 1),
('F7M-376', 'Suzuki', 1),
('U8H-801', 'Chevrolet', 1),
('KYY-157', 'Hyundai', 1),
('H0Z-423', 'Nissan', 1),
('M4C-316', 'Honda', 1),
('SWK-317', 'Toyota', 1);

INSERT INTO espacio_estacionamiento (id_zona, numero_espacio, fila, columna, estado, id_tipo_permitido) VALUES
(1, 'E-101', 1, 1, 'LIBRE', 1),
(1, 'E-102', 1, 2, 'LIBRE', 1),
(1, 'E-103', 1, 3, 'LIBRE', 1),
(1, 'E-104', 1, 4, 'LIBRE', 1),
(1, 'E-105', 1, 5, 'LIBRE', 1),
(1, 'E-106', 1, 6, 'LIBRE', 1),
(1, 'E-107', 1, 7, 'LIBRE', 1),
(1, 'E-108', 1, 8, 'LIBRE', 1),
(1, 'E-109', 1, 9, 'LIBRE', 1),
(1, 'E-110', 1, 10, 'LIBRE', 2),
(2, 'E-201', 2, 1, 'LIBRE', 1),
(2, 'E-202', 2, 2, 'LIBRE', 1),
(2, 'E-203', 2, 3, 'LIBRE', 1),
(2, 'E-204', 2, 4, 'LIBRE', 1),
(2, 'E-205', 2, 5, 'LIBRE', 1),
(2, 'E-206', 2, 6, 'LIBRE', 1),
(2, 'E-207', 2, 7, 'LIBRE', 1),
(2, 'E-208', 2, 8, 'LIBRE', 1),
(2, 'E-209', 2, 9, 'LIBRE', 1),
(2, 'E-210', 2, 10, 'LIBRE', 2),
(3, 'E-301', 3, 1, 'LIBRE', 1),
(3, 'E-302', 3, 2, 'LIBRE', 1),
(3, 'E-303', 3, 3, 'LIBRE', 1),
(3, 'E-304', 3, 4, 'LIBRE', 1),
(3, 'E-305', 3, 5, 'LIBRE', 1),
(3, 'E-306', 3, 6, 'LIBRE', 1),
(3, 'E-307', 3, 7, 'LIBRE', 1),
(3, 'E-308', 3, 8, 'LIBRE', 1),
(3, 'E-309', 3, 9, 'LIBRE', 1),
(3, 'E-310', 3, 10, 'LIBRE', 2),
(4, 'E-401', 4, 1, 'LIBRE', 1),
(4, 'E-402', 4, 2, 'LIBRE', 1),
(4, 'E-403', 4, 3, 'LIBRE', 1),
(4, 'E-404', 4, 4, 'LIBRE', 1),
(4, 'E-405', 4, 5, 'LIBRE', 1),
(4, 'E-406', 4, 6, 'LIBRE', 1),
(4, 'E-407', 4, 7, 'LIBRE', 1),
(4, 'E-408', 4, 8, 'LIBRE', 1),
(4, 'E-409', 4, 9, 'LIBRE', 1),
(4, 'E-410', 4, 10, 'LIBRE', 2),
(5, 'E-501', 5, 1, 'LIBRE', 1),
(5, 'E-502', 5, 2, 'LIBRE', 1),
(5, 'E-503', 5, 3, 'LIBRE', 1),
(5, 'E-504', 5, 4, 'LIBRE', 1),
(5, 'E-505', 5, 5, 'LIBRE', 1),
(5, 'E-506', 5, 6, 'LIBRE', 1),
(5, 'E-507', 5, 7, 'LIBRE', 1),
(5, 'E-508', 5, 8, 'LIBRE', 1),
(5, 'E-509', 5, 9, 'LIBRE', 1),
(5, 'E-510', 5, 10, 'LIBRE', 2);

INSERT INTO ticket (id_vehiculo, id_espacio, id_usuario_registro, fecha_hora_entrada, fecha_hora_salida, estado_ticket, monto_total) VALUES
(2, 16, 10, '2026-08-13 10:00:00', '2026-08-13 12:00:00', 'FINALIZADO', 8.00),
(24, 42, 19, '2026-08-02 19:00:00', '2026-08-02 22:15:00', 'FINALIZADO', 16.00),
(22, 2, 19, '2026-08-13 01:00:00', '2026-08-13 04:30:00', 'FINALIZADO', 16.00),
(17, 12, 17, '2026-08-05 20:00:00', '2026-08-06 00:30:00', 'FINALIZADO', 20.00),
(13, 30, 15, '2026-08-10 16:00:00', '2026-08-11 00:00:00', 'FINALIZADO', 16.00),
(8, 33, 10, '2026-08-02 16:00:00', '2026-08-02 22:00:00', 'FINALIZADO', 24.00),
(19, 19, 10, '2026-08-13 21:00:00', '2026-08-14 01:00:00', 'FINALIZADO', 16.00),
(3, 29, 3, '2026-08-06 05:00:00', '2026-08-06 07:00:00', 'FINALIZADO', 8.00),
(11, 26, 4, '2026-08-12 07:00:00', '2026-08-12 11:30:00', 'FINALIZADO', 20.00),
(22, 46, 19, '2026-08-05 21:00:00', '2026-08-06 00:45:00', 'FINALIZADO', 16.00),
(8, 23, 19, '2026-08-10 00:00:00', '2026-08-10 04:00:00', 'FINALIZADO', 16.00),
(4, 27, 17, '2026-08-08 21:00:00', '2026-08-09 04:45:00', 'FINALIZADO', 32.00),
(15, 10, 3, '2026-08-15 16:00:00', '2026-08-15 18:00:00', 'FINALIZADO', 4.00),
(13, 20, 14, '2026-08-03 15:00:00', '2026-08-03 19:15:00', 'FINALIZADO', 10.00),
(7, 3, 18, '2026-08-04 07:00:00', '2026-08-04 14:15:00', 'FINALIZADO', 32.00),
(9, 7, 18, '2026-08-06 15:00:00', '2026-08-06 17:45:00', 'FINALIZADO', 12.00),
(18, 21, 5, '2026-08-02 09:00:00', '2026-08-02 10:00:00', 'FINALIZADO', 4.00),
(25, 38, 10, '2026-08-04 21:00:00', '2026-08-05 04:45:00', 'FINALIZADO', 32.00),
(16, 31, 9, '2026-08-09 21:00:00', '2026-08-09 22:15:00', 'FINALIZADO', 8.00),
(13, 40, 2, '2026-08-09 15:00:00', '2026-08-09 20:45:00', 'FINALIZADO', 12.00),
(10, 45, 17, '2026-08-16 04:00:00', '2026-08-16 12:15:00', 'FINALIZADO', 36.00),
(7, 36, 13, '2026-08-05 23:00:00', '2026-08-06 00:00:00', 'FINALIZADO', 4.00),
(24, 9, 14, '2026-08-02 13:00:00', '2026-08-02 14:45:00', 'FINALIZADO', 8.00),
(17, 32, 20, '2026-08-04 16:00:00', '2026-08-04 17:00:00', 'FINALIZADO', 4.00),
(6, 24, 4, '2026-08-14 00:00:00', '2026-08-14 02:15:00', 'FINALIZADO', 12.00),
(13, 50, 5, NOW() - INTERVAL 3 HOUR, NULL, 'ACTIVO', NULL),
(8, 15, 3, NOW() - INTERVAL '2:15' HOUR_MINUTE, NULL, 'ACTIVO', NULL),
(3, 6, 17, NOW() - INTERVAL '1:40' HOUR_MINUTE, NULL, 'ACTIVO', NULL),
(19, 14, 20, NOW() - INTERVAL 45 MINUTE, NULL, 'ACTIVO', NULL),
(9, 48, 9, NOW() - INTERVAL 20 MINUTE, NULL, 'ACTIVO', NULL);

INSERT INTO pago (id_ticket, monto, metodo_pago, fecha_hora_pago, id_usuario_cobro) VALUES
(1, 8.00, 'YAPE', '2026-08-13 12:00:00', 10),
(2, 16.00, 'YAPE', '2026-08-02 22:15:00', 16),
(3, 16.00, 'TARJETA', '2026-08-13 04:30:00', 13),
(4, 20.00, 'PLIN', '2026-08-06 00:30:00', 14),
(5, 16.00, 'EFECTIVO', '2026-08-11 00:00:00', 2),
(6, 24.00, 'PLIN', '2026-08-02 22:00:00', 5),
(7, 16.00, 'EFECTIVO', '2026-08-14 01:00:00', 9),
(8, 8.00, 'YAPE', '2026-08-06 07:00:00', 6),
(9, 20.00, 'YAPE', '2026-08-12 11:30:00', 4),
(10, 16.00, 'TARJETA', '2026-08-06 00:45:00', 15),
(11, 16.00, 'YAPE', '2026-08-10 04:00:00', 7),
(12, 32.00, 'PLIN', '2026-08-09 04:45:00', 13),
(13, 4.00, 'EFECTIVO', '2026-08-15 18:00:00', 13),
(14, 10.00, 'EFECTIVO', '2026-08-03 19:15:00', 6),
(15, 32.00, 'YAPE', '2026-08-04 14:15:00', 5),
(16, 12.00, 'EFECTIVO', '2026-08-06 17:45:00', 6),
(17, 4.00, 'YAPE', '2026-08-02 10:00:00', 13),
(18, 32.00, 'TARJETA', '2026-08-05 04:45:00', 14),
(19, 8.00, 'TARJETA', '2026-08-09 22:15:00', 12),
(20, 12.00, 'PLIN', '2026-08-09 20:45:00', 12),
(21, 36.00, 'EFECTIVO', '2026-08-16 12:15:00', 4),
(22, 4.00, 'PLIN', '2026-08-06 00:00:00', 12),
(23, 8.00, 'EFECTIVO', '2026-08-02 14:45:00', 2),
(24, 4.00, 'YAPE', '2026-08-04 17:00:00', 6),
(25, 12.00, 'YAPE', '2026-08-14 02:15:00', 7);

-- Marcar como OCUPADO los espacios de los tickets actualmente ACTIVOS
UPDATE espacio_estacionamiento SET estado='OCUPADO' WHERE id_espacio IN (50,15,6,14,48);

-- =========================================================
-- PROCEDIMIENTOS ALMACENADOS
-- (el Arbol AVL y la Matriz viven en memoria dentro de Java;
--  MySQL persiste el resultado final de cada operacion)
-- =========================================================

DELIMITER $$

-- ---------------------------------------------------------
-- sp_registrar_entrada
-- Recibe el espacio YA ELEGIDO por la aplicacion Java (la Matriz
-- busca la fila con mas espacios libres). Busca o crea el vehiculo,
-- verifica que el espacio siga LIBRE, lo marca OCUPADO y crea el
-- ticket. Si el espacio ya no esta libre devuelve p_id_ticket = NULL
-- y no crea nada. No existe lista de espera: si no hay espacio, Java
-- informa "estacionamiento lleno" sin llamar a este procedimiento.
-- ---------------------------------------------------------
CREATE PROCEDURE sp_registrar_entrada (
    IN  p_placa       VARCHAR(10),
    IN  p_marca       VARCHAR(30),
    IN  p_id_tipo     INT,
    IN  p_id_usuario  INT,
    IN  p_id_espacio  INT,
    OUT p_id_ticket   INT
)
BEGIN
    DECLARE v_id_vehiculo INT DEFAULT NULL;
    DECLARE v_filas       INT DEFAULT 0;

    SET p_id_ticket = NULL;

    -- 1. Buscar o crear el vehiculo
    SELECT id_vehiculo INTO v_id_vehiculo
    FROM vehiculo WHERE placa = p_placa LIMIT 1;

    IF v_id_vehiculo IS NULL THEN
        INSERT INTO vehiculo (placa, marca, id_tipo)
        VALUES (p_placa, p_marca, p_id_tipo);
        SET v_id_vehiculo = LAST_INSERT_ID();
    END IF;

    -- 2. Ocupar el espacio solo si sigue LIBRE (evita asignarlo dos veces)
    UPDATE espacio_estacionamiento
    SET estado = 'OCUPADO'
    WHERE id_espacio = p_id_espacio AND estado = 'LIBRE';
    SET v_filas = ROW_COUNT();

    -- 3. Crear el ticket
    IF v_filas = 1 THEN
        INSERT INTO ticket (id_vehiculo, id_espacio, id_usuario_registro,
                             fecha_hora_entrada, estado_ticket)
        VALUES (v_id_vehiculo, p_id_espacio, p_id_usuario, NOW(), 'ACTIVO');
        SET p_id_ticket = LAST_INSERT_ID();
    END IF;
END$$

-- ---------------------------------------------------------
-- sp_registrar_salida
-- Cierra el ticket, calcula el monto (tarifa de TipoVehiculo
-- x horas, redondeando la fraccion hacia arriba), registra el
-- pago y libera el espacio.
-- ---------------------------------------------------------
CREATE PROCEDURE sp_registrar_salida (
    IN  p_id_ticket        INT,
    IN  p_id_usuario_cobro INT,
    IN  p_metodo_pago      VARCHAR(20),
    OUT p_monto            DECIMAL(8,2)
)
BEGIN
    DECLARE v_id_vehiculo   INT;
    DECLARE v_id_espacio    INT;
    DECLARE v_entrada       DATETIME;
    DECLARE v_id_tipo       INT;
    DECLARE v_precio_hora   DECIMAL(6,2);
    DECLARE v_horas         INT;

    SELECT id_vehiculo, id_espacio, fecha_hora_entrada
    INTO v_id_vehiculo, v_id_espacio, v_entrada
    FROM ticket WHERE id_ticket = p_id_ticket;

    SELECT id_tipo INTO v_id_tipo FROM vehiculo WHERE id_vehiculo = v_id_vehiculo;
    SELECT precio_hora INTO v_precio_hora FROM tipo_vehiculo WHERE id_tipo = v_id_tipo;

    SET v_horas = CEIL(TIMESTAMPDIFF(MINUTE, v_entrada, NOW()) / 60.0);
    IF v_horas < 1 THEN
        SET v_horas = 1;
    END IF;
    SET p_monto = v_horas * v_precio_hora;

    UPDATE ticket
    SET fecha_hora_salida = NOW(), monto_total = p_monto, estado_ticket = 'FINALIZADO'
    WHERE id_ticket = p_id_ticket;

    INSERT INTO pago (id_ticket, monto, metodo_pago, fecha_hora_pago, id_usuario_cobro)
    VALUES (p_id_ticket, p_monto, p_metodo_pago, NOW(), p_id_usuario_cobro);

    UPDATE espacio_estacionamiento
    SET estado = 'LIBRE'
    WHERE id_espacio = v_id_espacio;
END$$

-- ---------------------------------------------------------
-- sp_buscar_vehiculo_por_placa
-- Consulta de respaldo en BD (la busqueda "real" en tiempo de
-- ejecucion la hace el Arbol AVL en memoria dentro de Java).
-- ---------------------------------------------------------
CREATE PROCEDURE sp_buscar_vehiculo_por_placa (
    IN p_placa VARCHAR(10)
)
BEGIN
    SELECT v.id_vehiculo, v.placa, v.marca,
           t.id_ticket, t.estado_ticket, e.numero_espacio
    FROM vehiculo v
    LEFT JOIN ticket t ON t.id_vehiculo = v.id_vehiculo
                       AND t.estado_ticket = 'ACTIVO'
    LEFT JOIN espacio_estacionamiento e ON e.id_espacio = t.id_espacio
    WHERE v.placa = p_placa;
END$$

-- ---------------------------------------------------------
-- sp_reporte_ingresos_por_rango
-- Reporte parametrizado: ingresos totales entre dos fechas.
-- ---------------------------------------------------------
CREATE PROCEDURE sp_reporte_ingresos_por_rango (
    IN p_fecha_inicio DATE,
    IN p_fecha_fin    DATE
)
BEGIN
    SELECT DATE(p.fecha_hora_pago) AS fecha,
           COUNT(*) AS cantidad_tickets,
           SUM(p.monto) AS ingreso_total
    FROM pago p
    WHERE DATE(p.fecha_hora_pago) BETWEEN p_fecha_inicio AND p_fecha_fin
    GROUP BY DATE(p.fecha_hora_pago)
    ORDER BY fecha;
END$$

DELIMITER ;

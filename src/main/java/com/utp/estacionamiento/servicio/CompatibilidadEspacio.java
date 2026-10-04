package com.utp.estacionamiento.servicio;

import java.util.Set;

/**
 * Regla unica de compatibilidad entre tipos de vehiculo y espacios.
 *
 * El esquema aprobado guarda en cada espacio un id_tipo_permitido. En los
 * datos del estacionamiento solo hay dos clases de espacio:
 *   - id_tipo_permitido = 1 (Auto / Camioneta): espacios de auto, columnas 1 a 9
 *   - id_tipo_permitido = 2 (Moto): espacios de moto, columna 10
 *
 * Hay 3 tipos de vehiculo y se cobra segun el espacio que ocupan:
 * Auto / Camioneta va a espacios de auto; Moto y Bicicleta van a espacios
 * de moto.
 */
public final class CompatibilidadEspacio {

    /** Valor de id_tipo_permitido de los espacios de auto. */
    public static final int ESPACIO_AUTO = 1;
    /** Valor de id_tipo_permitido de los espacios de moto. */
    public static final int ESPACIO_MOTO = 2;

    /** Tipos que van a espacios de moto: Moto (2) y Bicicleta (3). */
    private static final Set<Integer> GRUPO_MOTO = Set.of(2, 3);

    private CompatibilidadEspacio() {}

    /** Devuelve el id_tipo_permitido de los espacios donde puede estacionar este tipo de vehiculo. */
    public static int tipoDeEspacioPara(int idTipoVehiculo) {
        return GRUPO_MOTO.contains(idTipoVehiculo) ? ESPACIO_MOTO : ESPACIO_AUTO;
    }

    public static String nombreGrupo(int idTipoEspacio) {
        return idTipoEspacio == ESPACIO_MOTO ? "moto" : "auto";
    }
}

package com.utp.estacionamiento.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Instancia unica y compartida de Gson para todos los Servlets.
 * Necesaria porque, a partir de JDK 17, la reflexion por defecto de
 * Gson no puede acceder a los campos internos de java.time.LocalDate
 * / LocalDateTime (el modulo java.base no los expone). Se registran
 * adaptadores explicitos que serializan estas fechas como texto ISO.
 */
public final class GsonProvider {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, type, ctx) ->
                            src == null ? null : new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .registerTypeAdapter(LocalDate.class,
                    (JsonDeserializer<LocalDate>) (json, type, ctx) ->
                            LocalDate.parse(json.getAsString()))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, type, ctx) ->
                            src == null ? null : new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, type, ctx) ->
                            LocalDateTime.parse(json.getAsString()))
            .create();

    private GsonProvider() {}

    public static Gson gson() {
        return GSON;
    }
}

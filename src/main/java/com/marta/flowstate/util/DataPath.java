package com.marta.flowstate.util;

import java.util.List;
import java.util.Map;

/**
 * Lee valores de los datos de una instancia con rutas separadas por puntos, por ejemplo
 * "validacion.body.ok" o "items.0.id". Si existe una clave literal con puntos, tiene prioridad.
 */
public final class DataPath {

    private DataPath() {
    }

    public static Object resolve(Map<String, Object> data, String path) {
        if (data == null || path == null) return null;
        if (data.containsKey(path)) return data.get(path);

        Object current = data;
        for (String part : path.split("\\.")) {
            if (current instanceof Map<?, ?> map) {
                current = map.get(part);
            } else if (current instanceof List<?> list && part.matches("\\d+")) {
                int index = Integer.parseInt(part);
                current = index < list.size() ? list.get(index) : null;
            } else {
                return null;
            }
            if (current == null) return null;
        }
        return current;
    }
}

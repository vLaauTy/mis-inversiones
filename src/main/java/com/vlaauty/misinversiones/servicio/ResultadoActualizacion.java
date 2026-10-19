package com.vlaauty.misinversiones.servicio;

import com.vlaauty.misinversiones.dominio.FuentePrecio;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Qué se pudo actualizar en una pasada. */
public record ResultadoActualizacion(Map<FuentePrecio, Integer> porFuente, List<String> sinPrecio,
                                     boolean dolarActualizado) {

    public int actualizados() {
        return porFuente.values().stream().mapToInt(Integer::intValue).sum();
    }

    /** "3 de BYMA, 1 de data912" */
    public String detalleFuentes() {
        return porFuente.entrySet().stream()
                .map(e -> e.getValue() + " de " + e.getKey().getEtiqueta())
                .collect(Collectors.joining(", "));
    }
}

package com.vlaauty.misinversiones.dominio;

public enum NivelRiesgo {
    BAJO("Bajo"),
    MEDIO("Medio"),
    ALTO("Alto");

    private final String etiqueta;

    NivelRiesgo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}

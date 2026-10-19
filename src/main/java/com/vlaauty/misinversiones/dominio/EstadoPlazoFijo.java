package com.vlaauty.misinversiones.dominio;

public enum EstadoPlazoFijo {
    ACTIVO("Activo"),
    COBRADO("Cobrado");

    private final String etiqueta;

    EstadoPlazoFijo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}

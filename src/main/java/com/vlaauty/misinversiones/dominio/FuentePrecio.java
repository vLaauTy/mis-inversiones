package com.vlaauty.misinversiones.dominio;

/** De dónde salió un precio. */
public enum FuentePrecio {
    MANUAL("Manual"),
    BYMA("BYMA"),
    DATA912("data912");

    private final String etiqueta;

    FuentePrecio(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}

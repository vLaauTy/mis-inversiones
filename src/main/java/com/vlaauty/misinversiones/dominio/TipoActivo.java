package com.vlaauty.misinversiones.dominio;

public enum TipoActivo {
    CEDEAR("CEDEAR"),
    ACCION("Acción argentina");

    private final String etiqueta;

    TipoActivo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}

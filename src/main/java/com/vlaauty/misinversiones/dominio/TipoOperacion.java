package com.vlaauty.misinversiones.dominio;

public enum TipoOperacion {
    COMPRA("Compra"),
    VENTA("Venta");

    private final String etiqueta;

    TipoOperacion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}

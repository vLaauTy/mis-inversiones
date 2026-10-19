package com.vlaauty.misinversiones.dominio;

public enum TipoMovimiento {
    DEPOSITO("Depósito"),
    RETIRO("Retiro"),
    INTERES("Interés");

    private final String etiqueta;

    TipoMovimiento(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}

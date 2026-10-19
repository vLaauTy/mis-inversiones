package com.vlaauty.misinversiones.dominio;

public enum TipoBolsita {
    RESERVA("Reserva / remunerada"),
    EMERGENCIA("Fondo de emergencia"),
    PLAZO_FIJO("Plazos fijos / LECAP"),
    OTRA("Otra");

    private final String etiqueta;

    TipoBolsita(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}

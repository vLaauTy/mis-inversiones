package com.vlaauty.misinversiones.servicio;

/** Utilidades chicas para texto que viene de formularios. */
final class Textos {

    private Textos() {
    }

    static String vacioANull(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}

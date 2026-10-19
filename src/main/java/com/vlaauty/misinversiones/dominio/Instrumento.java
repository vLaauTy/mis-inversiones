package com.vlaauty.misinversiones.dominio;

public enum Instrumento {
    PLAZO_FIJO("Plazo fijo", true),
    PLAZO_FIJO_UVA("Plazo fijo UVA", false),
    LECAP("LECAP", true);

    private final String etiqueta;
    /** true si el interés se puede estimar con la tasa fija; el UVA depende de la inflación. */
    private final boolean tasaFija;

    Instrumento(String etiqueta, boolean tasaFija) {
        this.etiqueta = etiqueta;
        this.tasaFija = tasaFija;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public boolean isTasaFija() {
        return tasaFija;
    }
}

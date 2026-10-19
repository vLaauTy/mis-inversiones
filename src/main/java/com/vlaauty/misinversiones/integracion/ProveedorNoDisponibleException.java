package com.vlaauty.misinversiones.integracion;

/** Una fuente externa no respondió o respondió algo inesperado. */
public class ProveedorNoDisponibleException extends RuntimeException {

    public ProveedorNoDisponibleException(String fuente, Throwable causa) {
        super(fuente + " no disponible: " + causa.getMessage(), causa);
    }
}

package com.vlaauty.misinversiones.servicio;

/** Error de validación que se muestra al usuario tal cual (ej. "No tenés tantos CEDEARs para vender"). */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}

package com.vlaauty.misinversiones.integracion;

import com.vlaauty.misinversiones.dominio.FuentePrecio;
import com.vlaauty.misinversiones.dominio.TipoActivo;

import java.util.Map;

/** Una fuente de precios de BYMA. Cada implementación trae el panel completo de un tipo de activo. */
public interface ProveedorCotizaciones {

    FuentePrecio fuente();

    /**
     * Cotizaciones en pesos del panel, por ticker en mayúsculas.
     *
     * @throws ProveedorNoDisponibleException si la fuente no responde o responde algo que no se entiende
     */
    Map<String, Cotizacion> cotizaciones(TipoActivo tipo);
}

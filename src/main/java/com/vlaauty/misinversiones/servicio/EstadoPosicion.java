package com.vlaauty.misinversiones.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Tenencia de un activo después de recorrer sus operaciones con el método de costo promedio.
 * El costo incluye las comisiones de compra.
 */
public record EstadoPosicion(BigDecimal cantidad, BigDecimal costo, BigDecimal gananciaRealizada,
                             BigDecimal ultimoPrecioOperado) {

    public static EstadoPosicion vacia() {
        return new EstadoPosicion(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null);
    }

    public boolean tieneTenencia() {
        return cantidad.signum() > 0;
    }

    public BigDecimal costoPromedio() {
        if (!tieneTenencia()) {
            return BigDecimal.ZERO;
        }
        return costo.divide(cantidad, 4, RoundingMode.HALF_UP);
    }
}

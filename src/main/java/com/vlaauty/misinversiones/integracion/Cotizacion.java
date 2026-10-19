package com.vlaauty.misinversiones.integracion;

import java.math.BigDecimal;

/**
 * Precio de un ticker según una fuente externa.
 *
 * @param precio          último precio en pesos
 * @param variacionDiaria variación contra el cierre anterior, como fracción (-0.0123 = -1,23%); puede faltar
 */
public record Cotizacion(String ticker, BigDecimal precio, BigDecimal variacionDiaria) {
}

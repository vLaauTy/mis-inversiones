package com.vlaauty.misinversiones.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Interés simple con TNA, como lo calculan los bancos argentinos para plazos fijos (base 365 días). */
public final class CalculoInteres {

    private static final BigDecimal DIAS_ANIO = BigDecimal.valueOf(365);

    private CalculoInteres() {
    }

    /** @param tna fracción (0.22 = 22%) */
    public static BigDecimal simple(BigDecimal monto, BigDecimal tna, long dias) {
        return monto.multiply(tna)
                .multiply(BigDecimal.valueOf(dias))
                .divide(DIAS_ANIO, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal diario(BigDecimal monto, BigDecimal tna) {
        return simple(monto, tna, 1);
    }
}

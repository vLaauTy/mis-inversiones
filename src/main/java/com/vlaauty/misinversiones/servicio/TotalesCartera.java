package com.vlaauty.misinversiones.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Suma de lo invertido y lo que vale hoy la cartera. */
public record TotalesCartera(BigDecimal invertido, BigDecimal valor) {

    public static TotalesCartera de(List<Posicion> posiciones) {
        BigDecimal invertido = posiciones.stream().map(Posicion::invertido).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valor = posiciones.stream().map(Posicion::valorActual).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new TotalesCartera(invertido, valor);
    }

    public BigDecimal ganancia() {
        return valor.subtract(invertido);
    }

    /** Ganancia como fracción de lo invertido (0.016 = 1,6%). */
    public BigDecimal gananciaPct() {
        if (invertido.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return ganancia().divide(invertido, 6, RoundingMode.HALF_UP);
    }
}

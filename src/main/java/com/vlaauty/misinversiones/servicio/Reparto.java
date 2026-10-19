package com.vlaauty.misinversiones.servicio;

import com.vlaauty.misinversiones.dominio.NivelRiesgo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/** Cómo están repartidas las acciones por riesgo contra el objetivo del plan. */
public record Reparto(List<Nivel> niveles, Optional<Sugerencia> sugerencia) {

    /** @param pctReal fracción (0.53 = 53%); objetivo en puntos (50) */
    public record Nivel(NivelRiesgo nivel, BigDecimal valor, BigDecimal pctReal, int objetivo) {

        public int pctRealEntero() {
            return pctReal.movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValue();
        }

        public int desvioPuntos() {
            return pctRealEntero() - objetivo;
        }
    }

    /** Cuánta plata nueva poner en un nivel para que vuelva a su objetivo. */
    public record Sugerencia(NivelRiesgo nivel, int puntosAbajo, BigDecimal monto) {
    }
}

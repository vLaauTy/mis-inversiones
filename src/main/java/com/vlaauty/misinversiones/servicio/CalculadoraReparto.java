package com.vlaauty.misinversiones.servicio;

import com.vlaauty.misinversiones.dominio.NivelRiesgo;
import com.vlaauty.misinversiones.dominio.PlanInversion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Compara el reparto real de las acciones con el objetivo y sugiere la próxima compra. */
public final class CalculadoraReparto {

    /** Desvío mínimo, en puntos, para sugerir rebalancear. */
    static final int UMBRAL_PUNTOS = 2;

    private CalculadoraReparto() {
    }

    public static Reparto calcular(List<Posicion> posiciones, PlanInversion plan) {
        Map<NivelRiesgo, BigDecimal> valores = new EnumMap<>(NivelRiesgo.class);
        for (NivelRiesgo n : NivelRiesgo.values()) {
            valores.put(n, BigDecimal.ZERO);
        }
        for (Posicion p : posiciones) {
            valores.merge(p.activo().getNivelRiesgo(), p.valorActual(), BigDecimal::add);
        }
        BigDecimal total = valores.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Reparto.Nivel> niveles = new ArrayList<>();
        for (NivelRiesgo n : NivelRiesgo.values()) {
            BigDecimal pct = total.signum() == 0
                    ? BigDecimal.ZERO
                    : valores.get(n).divide(total, 6, RoundingMode.HALF_UP);
            niveles.add(new Reparto.Nivel(n, valores.get(n), pct, plan.objetivoDe(n)));
        }
        if (total.signum() == 0) {
            return new Reparto(niveles, Optional.empty());
        }

        Optional<Reparto.Sugerencia> sugerencia = niveles.stream()
                .filter(n -> -n.desvioPuntos() >= UMBRAL_PUNTOS && n.objetivo() < 100)
                .max(Comparator.comparingInt(n -> -n.desvioPuntos()))
                .map(n -> {
                    // Plata nueva M en el nivel: (v + M) / (T + M) = o  =>  M = (o·T − v) / (1 − o)
                    BigDecimal o = BigDecimal.valueOf(n.objetivo()).movePointLeft(2);
                    BigDecimal monto = o.multiply(total).subtract(n.valor())
                            .divide(BigDecimal.ONE.subtract(o), 0, RoundingMode.HALF_UP);
                    return new Reparto.Sugerencia(n.nivel(), -n.desvioPuntos(), monto);
                });
        return new Reparto(niveles, sugerencia);
    }
}

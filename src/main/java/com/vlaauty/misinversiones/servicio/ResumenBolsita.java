package com.vlaauty.misinversiones.servicio;

import com.vlaauty.misinversiones.dominio.Bolsita;
import com.vlaauty.misinversiones.dominio.CalculoInteres;
import com.vlaauty.misinversiones.dominio.PlazoFijo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Saldo de una bolsita: depósitos − retiros + intereses cobrados + plazos fijos activos.
 * El rendimiento diario de una reserva es una estimación con la tasa cargada.
 */
public record ResumenBolsita(Bolsita bolsita, BigDecimal saldoMovimientos, List<PlazoFijo> plazosActivos,
                             LocalDate hoy) {

    public BigDecimal enPlazosFijos() {
        return plazosActivos.stream().map(PlazoFijo::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal saldo() {
        return saldoMovimientos.add(enPlazosFijos());
    }

    /** Lo que rinde por día lo que está en la reserva (no incluye plazos fijos). */
    public BigDecimal rindePorDia() {
        if (bolsita.getTasa() == null || saldoMovimientos.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return CalculoInteres.diario(saldoMovimientos, bolsita.getTasa());
    }

    public Long diasParaUso() {
        if (bolsita.getFechaUso() == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(hoy, bolsita.getFechaUso());
    }

    /** Interés estimado desde hoy hasta la fecha de uso, con la tasa actual. */
    public BigDecimal estimadoHastaUso() {
        Long dias = diasParaUso();
        if (dias == null || dias <= 0) {
            return BigDecimal.ZERO;
        }
        return rindePorDia().multiply(BigDecimal.valueOf(dias));
    }

    /** Avance contra la meta, como entero de 0 a 100 (null si no hay meta). */
    public Integer avanceMeta() {
        if (bolsita.getMeta() == null || bolsita.getMeta().signum() <= 0) {
            return null;
        }
        BigDecimal pct = saldo().multiply(BigDecimal.valueOf(100)).divide(bolsita.getMeta(), 0, RoundingMode.HALF_UP);
        return Math.max(0, Math.min(100, pct.intValue()));
    }
}

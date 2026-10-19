package com.vlaauty.misinversiones.web;

import com.vlaauty.misinversiones.dominio.NivelRiesgo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formatos argentinos para las vistas: ${@fmt.moneda(x)}, ${@fmt.pct(x)}… */
@Component("fmt")
public class Formato {

    private static final Locale AR = Locale.forLanguageTag("es-AR");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private DecimalFormat patron(String p) {
        DecimalFormatSymbols s = new DecimalFormatSymbols(AR);
        s.setDecimalSeparator(',');
        s.setGroupingSeparator('.');
        return new DecimalFormat(p, s);
    }

    /** $1.234.567 (sin centavos desde $1.000; con centavos por debajo). */
    public String moneda(BigDecimal v) {
        if (v == null) {
            return "—";
        }
        String signo = v.signum() < 0 ? "-" : "";
        BigDecimal abs = v.abs();
        String cuerpo = abs.compareTo(BigDecimal.valueOf(1000)) >= 0
                ? patron("#,##0").format(abs.setScale(0, RoundingMode.HALF_UP))
                : patron("#,##0.00").format(abs.setScale(2, RoundingMode.HALF_UP));
        return signo + "$" + cuerpo;
    }

    /** Con signo: +$2.140 / -$500. */
    public String monedaConSigno(BigDecimal v) {
        if (v == null) {
            return "—";
        }
        return (v.signum() > 0 ? "+" : "") + moneda(v);
    }

    /** Fracción a porcentaje con signo: 0.016 → +1,6% */
    public String pct(BigDecimal fraccion) {
        if (fraccion == null) {
            return "—";
        }
        BigDecimal p = fraccion.movePointRight(2).setScale(1, RoundingMode.HALF_UP);
        return (p.signum() > 0 ? "+" : "") + patron("0.0").format(p) + "%";
    }

    /** Tasa (fracción) sin signo: 0.19 → 19% ; 0.225 → 22,5% */
    public String tasa(BigDecimal fraccion) {
        if (fraccion == null) {
            return "—";
        }
        BigDecimal p = fraccion.movePointRight(2).stripTrailingZeros();
        return patron("0.##").format(p) + "%";
    }

    public String cantidad(BigDecimal v) {
        return v == null ? "—" : patron("#,##0.######").format(v);
    }

    public String fecha(LocalDate f) {
        return f == null ? "—" : f.format(FECHA);
    }

    public String fechaHora(LocalDateTime f) {
        return f == null ? "—" : f.format(FECHA_HORA);
    }

    /** Para pre-cargar inputs numéricos: 0.19 → "19" ; 500000.0000 → "500000" */
    public String plano(BigDecimal v) {
        return v == null ? "" : v.stripTrailingZeros().toPlainString();
    }

    public String tasaPlano(BigDecimal fraccion) {
        return fraccion == null ? "" : fraccion.movePointRight(2).stripTrailingZeros().toPlainString();
    }

    /** Clase CSS del color de cada nivel de riesgo: bajo, medio, alto. */
    public String claseNivel(NivelRiesgo nivel) {
        return nivel.name().toLowerCase(Locale.ROOT);
    }

    /** Clase CSS según el signo: sube, baja o neutro. */
    public String claseSigno(BigDecimal v) {
        if (v == null || v.signum() == 0) {
            return "neutro";
        }
        return v.signum() > 0 ? "sube" : "baja";
    }
}

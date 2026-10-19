package com.vlaauty.misinversiones.servicio;

import com.vlaauty.misinversiones.dominio.Activo;
import com.vlaauty.misinversiones.dominio.FuentePrecio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Una línea de la cartera con su valor actual.
 *
 * @param variacionDiaria variación del día que informó la fuente (fracción); null en precios manuales
 */
public record Posicion(Activo activo, BigDecimal cantidad, BigDecimal invertido, BigDecimal precioActual,
                       FuentePrecio fuentePrecio, LocalDateTime fechaPrecio, BigDecimal variacionDiaria) {

    public BigDecimal valorActual() {
        return cantidad.multiply(precioActual);
    }

    public BigDecimal ganancia() {
        return valorActual().subtract(invertido);
    }

    public BigDecimal costoPromedio() {
        return invertido.divide(cantidad, 2, RoundingMode.HALF_UP);
    }

    /** Ganancia como fracción del invertido (0.016 = 1,6%). */
    public BigDecimal gananciaPct() {
        if (invertido.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return ganancia().divide(invertido, 6, RoundingMode.HALF_UP);
    }

    /** La variación del día solo tiene sentido si el precio es de hoy. */
    public boolean tieneVariacionDe(LocalDate hoy) {
        return variacionDiaria != null && fechaPrecio != null && fechaPrecio.toLocalDate().equals(hoy);
    }

    /** true cuando no hay precio cargado y se usa el de la última operación. */
    public boolean precioDeOperacion() {
        return fuentePrecio == null;
    }
}

package com.vlaauty.misinversiones.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

/**
 * Propiedades bajo "app" en application.yml. Se validan al arrancar: si falta algo, la app no levanta.
 *
 * @param claveRecordarme secreto para firmar la cookie "recordarme"; en prod tiene que ser fijo
 */
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(@NotBlank String usuario,
                            @NotBlank String password,
                            @NotBlank String claveRecordarme,
                            @NotBlank String zonaHoraria,
                            @NotNull @Valid Comision comision) {

    /** Comisión del broker sobre el monto operado, más IVA sobre la comisión. Fracciones (0.0045 = 0,45%). */
    public record Comision(@NotNull @DecimalMin("0") @DecimalMax("0.05") BigDecimal porcentaje,
                           @NotNull @DecimalMin("0") @DecimalMax("1") BigDecimal iva) {
    }
}

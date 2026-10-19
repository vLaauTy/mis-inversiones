package com.vlaauty.misinversiones.config;

import com.vlaauty.misinversiones.dominio.FuentePrecio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Propiedades bajo "app.precios".
 *
 * @param automaticos si es false no se programa nada (los tests y quien quiera cargar a mano)
 * @param cron        cuándo se actualizan, en hora de Buenos Aires
 * @param fuentes     orden en que se consultan; si una falla o no tiene el ticker, se pasa a la siguiente
 */
@Validated
@ConfigurationProperties(prefix = "app.precios")
public record PreciosProperties(boolean automaticos,
                                @NotBlank String cron,
                                @NotEmpty List<FuentePrecio> fuentes,
                                @NotBlank String bymaUrl,
                                @NotBlank String data912Url,
                                @NotBlank String dolarUrl) {
}

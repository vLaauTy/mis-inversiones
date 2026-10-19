package com.vlaauty.misinversiones.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Tareas programadas (precios automáticos). Se apagan con app.precios.automaticos=false. */
@Configuration
@EnableScheduling
@ConditionalOnBooleanProperty("app.precios.automaticos")
public class ProgramacionConfig {
}

package com.vlaauty.misinversiones.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class RelojConfig {

    /** Reloj en hora de Buenos Aires; los tests pueden reemplazarlo por uno fijo. */
    @Bean
    public Clock reloj(AppProperties propiedades) {
        return Clock.system(ZoneId.of(propiedades.zonaHoraria()));
    }
}

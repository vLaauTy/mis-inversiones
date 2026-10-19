package com.vlaauty.misinversiones.servicio;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Corre el actualizador cada 15 minutos en horario de mercado (ver app.precios.cron)
 * y una vez al arrancar, así al prender la PC ya tenés el último cierre.
 */
@Component
@ConditionalOnBooleanProperty("app.precios.automaticos")
public class PreciosProgramados {

    private final ActualizadorPrecios actualizador;
    private final TaskScheduler programador;

    public PreciosProgramados(ActualizadorPrecios actualizador, TaskScheduler programador) {
        this.actualizador = actualizador;
        this.programador = programador;
    }

    @Scheduled(cron = "${app.precios.cron}", zone = "${app.zona-horaria}")
    public void enHorarioDeMercado() {
        actualizador.actualizar();
    }

    /** En otro hilo, para no demorar el arranque si alguna fuente tarda. */
    @EventListener(ApplicationReadyEvent.class)
    public void alArrancar() {
        programador.schedule(actualizador::actualizar, Instant.now());
    }
}

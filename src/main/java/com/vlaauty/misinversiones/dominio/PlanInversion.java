package com.vlaauty.misinversiones.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Reparto objetivo de las acciones por nivel de riesgo (porcentajes enteros que suman 100). */
@Entity
@Table(name = "plan_inversion")
public class PlanInversion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String perfil;

    @Column(name = "objetivo_bajo", nullable = false)
    private int objetivoBajo;

    @Column(name = "objetivo_medio", nullable = false)
    private int objetivoMedio;

    @Column(name = "objetivo_alto", nullable = false)
    private int objetivoAlto;

    @Column(name = "aporte_mensual", precision = 19, scale = 4)
    private BigDecimal aporteMensual;

    protected PlanInversion() {
    }

    public PlanInversion(String perfil, int objetivoBajo, int objetivoMedio, int objetivoAlto) {
        this.perfil = perfil;
        this.objetivoBajo = objetivoBajo;
        this.objetivoMedio = objetivoMedio;
        this.objetivoAlto = objetivoAlto;
    }

    public int objetivoDe(NivelRiesgo nivel) {
        return switch (nivel) {
            case BAJO -> objetivoBajo;
            case MEDIO -> objetivoMedio;
            case ALTO -> objetivoAlto;
        };
    }

    public Long getId() {
        return id;
    }

    public String getPerfil() {
        return perfil;
    }

    public int getObjetivoBajo() {
        return objetivoBajo;
    }

    public int getObjetivoMedio() {
        return objetivoMedio;
    }

    public int getObjetivoAlto() {
        return objetivoAlto;
    }

    public BigDecimal getAporteMensual() {
        return aporteMensual;
    }
}

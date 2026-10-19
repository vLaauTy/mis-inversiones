package com.vlaauty.misinversiones.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Precio de un activo en un momento. En la etapa 1 se carga a mano; después llega de BYMA. */
@Entity
public class Precio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activo_id")
    private Activo activo;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "precio_ars", nullable = false, precision = 19, scale = 4)
    private BigDecimal precioArs;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FuentePrecio fuente;

    protected Precio() {
    }

    public Precio(Activo activo, LocalDateTime fechaHora, BigDecimal precioArs, FuentePrecio fuente) {
        this.activo = activo;
        this.fechaHora = fechaHora;
        this.precioArs = precioArs;
        this.fuente = fuente;
    }

    public Long getId() {
        return id;
    }

    public Activo getActivo() {
        return activo;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public BigDecimal getPrecioArs() {
        return precioArs;
    }

    public FuentePrecio getFuente() {
        return fuente;
    }
}

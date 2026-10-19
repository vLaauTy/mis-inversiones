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

/** Precio de un activo en un momento: cargado a mano o traído de BYMA / data912. */
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

    /** Variación del día según la fuente, como fracción. Vacía en los precios manuales. */
    @Column(name = "variacion_diaria", precision = 9, scale = 6)
    private BigDecimal variacionDiaria;

    protected Precio() {
    }

    public Precio(Activo activo, LocalDateTime fechaHora, BigDecimal precioArs, FuentePrecio fuente) {
        this(activo, fechaHora, precioArs, fuente, null);
    }

    public Precio(Activo activo, LocalDateTime fechaHora, BigDecimal precioArs, FuentePrecio fuente,
                  BigDecimal variacionDiaria) {
        this.activo = activo;
        this.fechaHora = fechaHora;
        this.precioArs = precioArs;
        this.fuente = fuente;
        this.variacionDiaria = variacionDiaria;
    }

    /** true si es el mismo precio de la misma fuente y el mismo día (evita repetir el cierre). */
    public boolean repite(BigDecimal otroPrecio, FuentePrecio otraFuente, LocalDateTime otraFechaHora) {
        return fuente == otraFuente
                && precioArs.compareTo(otroPrecio) == 0
                && fechaHora.toLocalDate().equals(otraFechaHora.toLocalDate());
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

    public BigDecimal getVariacionDiaria() {
        return variacionDiaria;
    }
}

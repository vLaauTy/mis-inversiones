package com.vlaauty.misinversiones.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Cotización del dólar contado con liquidación, para mostrar la cartera en dólares. */
@Entity
@Table(name = "dolar_ccl")
public class DolarCcl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal compra;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal venta;

    protected DolarCcl() {
    }

    public DolarCcl(LocalDateTime fechaHora, BigDecimal compra, BigDecimal venta) {
        this.fechaHora = fechaHora;
        this.compra = compra;
        this.venta = venta;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public BigDecimal getCompra() {
        return compra;
    }

    /** Precio al que comprás dólares: el que sirve para pasar pesos a dólares. */
    public BigDecimal getVenta() {
        return venta;
    }
}

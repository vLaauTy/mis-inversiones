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
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Depósito, retiro o interés cobrado en una bolsita. */
@Entity
@Table(name = "movimiento_bolsita")
public class MovimientoBolsita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bolsita_id")
    private Bolsita bolsita;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimiento tipo;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal monto;

    private String nota;

    protected MovimientoBolsita() {
    }

    public MovimientoBolsita(Bolsita bolsita, LocalDate fecha, TipoMovimiento tipo, BigDecimal monto, String nota) {
        this.bolsita = bolsita;
        this.fecha = fecha;
        this.tipo = tipo;
        this.monto = monto;
        this.nota = nota;
    }

    /** Monto con signo: los retiros restan. */
    public BigDecimal getMontoConSigno() {
        return tipo == TipoMovimiento.RETIRO ? monto.negate() : monto;
    }

    public Long getId() {
        return id;
    }

    public Bolsita getBolsita() {
        return bolsita;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getNota() {
        return nota;
    }
}

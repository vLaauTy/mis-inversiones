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
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Compra o venta de un activo. La cartera se calcula siempre a partir de estas filas. */
@Entity
public class Operacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activo_id")
    private Activo activo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoOperacion tipo;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 19, scale = 4)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal comision;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrigenOperacion origen;

    private String nota;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;

    protected Operacion() {
    }

    public Operacion(LocalDate fecha, Activo activo, TipoOperacion tipo, BigDecimal cantidad,
                     BigDecimal precioUnitario, BigDecimal comision, OrigenOperacion origen, String nota) {
        this.fecha = fecha;
        this.activo = activo;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.comision = comision;
        this.origen = origen;
        this.nota = nota;
        this.creadaEn = LocalDateTime.now();
    }

    /** Lo que pagaste (compra) o recibiste (venta), con la comisión incluida. */
    public BigDecimal getTotal() {
        BigDecimal bruto = cantidad.multiply(precioUnitario);
        return tipo == TipoOperacion.COMPRA ? bruto.add(comision) : bruto.subtract(comision);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Activo getActivo() {
        return activo;
    }

    public TipoOperacion getTipo() {
        return tipo;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getComision() {
        return comision;
    }

    public OrigenOperacion getOrigen() {
        return origen;
    }

    public String getNota() {
        return nota;
    }

    public LocalDateTime getCreadaEn() {
        return creadaEn;
    }
}

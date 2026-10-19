package com.vlaauty.misinversiones.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Plata separada con un fin: Black Friday, emergencia, enero en adelante… */
@Entity
public class Bolsita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoBolsita tipo;

    /** Dónde está la plata (ej. Mercado Pago). */
    private String entidad;

    /** TNA actual como fracción (0.19 = 19%). Solo para reservas o cuentas remuneradas. */
    @Column(precision = 9, scale = 6)
    private BigDecimal tasa;

    @Column(precision = 19, scale = 4)
    private BigDecimal meta;

    @Column(name = "fecha_uso")
    private LocalDate fechaUso;

    protected Bolsita() {
    }

    public Bolsita(String nombre, TipoBolsita tipo, String entidad, BigDecimal tasa, BigDecimal meta, LocalDate fechaUso) {
        this.nombre = nombre.trim();
        this.tipo = tipo;
        this.entidad = entidad;
        this.tasa = tasa;
        this.meta = meta;
        this.fechaUso = fechaUso;
    }

    public void actualizar(String nombre, TipoBolsita tipo, String entidad, BigDecimal tasa, BigDecimal meta, LocalDate fechaUso) {
        this.nombre = nombre.trim();
        this.tipo = tipo;
        this.entidad = entidad;
        this.tasa = tasa;
        this.meta = meta;
        this.fechaUso = fechaUso;
    }

    public void setTasa(BigDecimal tasa) {
        this.tasa = tasa;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoBolsita getTipo() {
        return tipo;
    }

    public String getEntidad() {
        return entidad;
    }

    public BigDecimal getTasa() {
        return tasa;
    }

    public BigDecimal getMeta() {
        return meta;
    }

    public LocalDate getFechaUso() {
        return fechaUso;
    }
}

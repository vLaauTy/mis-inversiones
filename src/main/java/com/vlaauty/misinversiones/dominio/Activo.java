package com.vlaauty.misinversiones.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Una acción o CEDEAR que tenés o pensás comprar. */
@Entity
public class Activo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String ticker;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoActivo tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_riesgo", nullable = false, length = 10)
    private NivelRiesgo nivelRiesgo;

    protected Activo() {
    }

    public Activo(String ticker, String nombre, TipoActivo tipo, NivelRiesgo nivelRiesgo) {
        this.ticker = ticker.trim().toUpperCase();
        this.nombre = nombre.trim();
        this.tipo = tipo;
        this.nivelRiesgo = nivelRiesgo;
    }

    public void actualizar(String nombre, TipoActivo tipo, NivelRiesgo nivelRiesgo) {
        this.nombre = nombre.trim();
        this.tipo = tipo;
        this.nivelRiesgo = nivelRiesgo;
    }

    public Long getId() {
        return id;
    }

    public String getTicker() {
        return ticker;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoActivo getTipo() {
        return tipo;
    }

    public NivelRiesgo getNivelRiesgo() {
        return nivelRiesgo;
    }
}

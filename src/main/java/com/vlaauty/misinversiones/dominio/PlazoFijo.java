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
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/** Plazo fijo tradicional, UVA o LECAP. */
@Entity
@Table(name = "plazo_fijo")
public class PlazoFijo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bolsita_id")
    private Bolsita bolsita;

    @Column(nullable = false, length = 80)
    private String entidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Instrumento instrumento;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal monto;

    /** TNA como fracción (0.22 = 22%). En UVA es la tasa por encima de la inflación. */
    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal tasa;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoPlazoFijo estado;

    @Column(name = "monto_cobrado", precision = 19, scale = 4)
    private BigDecimal montoCobrado;

    protected PlazoFijo() {
    }

    public PlazoFijo(Bolsita bolsita, String entidad, Instrumento instrumento, BigDecimal monto, BigDecimal tasa,
                     LocalDate fechaInicio, LocalDate fechaVencimiento) {
        this.bolsita = bolsita;
        this.entidad = entidad.trim();
        this.instrumento = instrumento;
        this.monto = monto;
        this.tasa = tasa;
        this.fechaInicio = fechaInicio;
        this.fechaVencimiento = fechaVencimiento;
        this.estado = EstadoPlazoFijo.ACTIVO;
    }

    public long getDias() {
        return ChronoUnit.DAYS.between(fechaInicio, fechaVencimiento);
    }

    /** Interés estimado al vencimiento. Vacío para UVA, que depende de la inflación. */
    public Optional<BigDecimal> getInteresEstimado() {
        if (!instrumento.isTasaFija()) {
            return Optional.empty();
        }
        return Optional.of(CalculoInteres.simple(monto, tasa, getDias()));
    }

    public Optional<BigDecimal> getTotalAlVencer() {
        return getInteresEstimado().map(monto::add);
    }

    public long diasParaVencer(LocalDate hoy) {
        return ChronoUnit.DAYS.between(hoy, fechaVencimiento);
    }

    public void marcarCobrado(BigDecimal montoCobrado) {
        this.estado = EstadoPlazoFijo.COBRADO;
        this.montoCobrado = montoCobrado;
    }

    public boolean isActivo() {
        return estado == EstadoPlazoFijo.ACTIVO;
    }

    public Long getId() {
        return id;
    }

    public Bolsita getBolsita() {
        return bolsita;
    }

    public String getEntidad() {
        return entidad;
    }

    public Instrumento getInstrumento() {
        return instrumento;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public BigDecimal getTasa() {
        return tasa;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public EstadoPlazoFijo getEstado() {
        return estado;
    }

    public BigDecimal getMontoCobrado() {
        return montoCobrado;
    }
}

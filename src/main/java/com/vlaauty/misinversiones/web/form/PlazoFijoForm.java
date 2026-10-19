package com.vlaauty.misinversiones.web.form;

import com.vlaauty.misinversiones.dominio.Instrumento;
import com.vlaauty.misinversiones.servicio.BolsitaService.NuevoPlazoFijo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Formulario de plazo fijo, plazo fijo UVA o LECAP. La tasa se carga en porcentaje. */
public class PlazoFijoForm {

    @NotNull(message = "Elegí la bolsita")
    private Long bolsitaId;

    @NotNull
    private Instrumento instrumento = Instrumento.PLAZO_FIJO;

    @NotBlank(message = "Poné el banco o broker")
    @Size(max = 80)
    private String entidad;

    @NotNull(message = "Poné el monto")
    @Positive(message = "El monto tiene que ser mayor a cero")
    private BigDecimal monto;

    /** En porcentaje: 22 = 22% TNA. */
    @NotNull(message = "Poné la tasa")
    @PositiveOrZero(message = "La tasa no puede ser negativa")
    @Max(value = 500, message = "Revisá la tasa: va en porcentaje (22 = 22%)")
    private BigDecimal tasaPct;

    @NotNull(message = "Poné la fecha de inicio")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInicio;

    @NotNull(message = "Poné el plazo en días")
    @Positive(message = "El plazo tiene que ser de al menos un día")
    private Integer plazoDias = 30;

    public NuevoPlazoFijo aNuevoPlazoFijo() {
        return new NuevoPlazoFijo(bolsitaId, entidad, instrumento, monto, Tasas.aFraccion(tasaPct), fechaInicio, plazoDias);
    }

    public Long getBolsitaId() { return bolsitaId; }
    public void setBolsitaId(Long bolsitaId) { this.bolsitaId = bolsitaId; }
    public Instrumento getInstrumento() { return instrumento; }
    public void setInstrumento(Instrumento instrumento) { this.instrumento = instrumento; }
    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public BigDecimal getTasaPct() { return tasaPct; }
    public void setTasaPct(BigDecimal tasaPct) { this.tasaPct = tasaPct; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public Integer getPlazoDias() { return plazoDias; }
    public void setPlazoDias(Integer plazoDias) { this.plazoDias = plazoDias; }
}

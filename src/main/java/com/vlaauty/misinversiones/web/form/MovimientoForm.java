package com.vlaauty.misinversiones.web.form;

import com.vlaauty.misinversiones.dominio.TipoMovimiento;
import com.vlaauty.misinversiones.servicio.BolsitaService.NuevoMovimiento;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Formulario de depósito, retiro o interés en una bolsita. La tasa se carga en porcentaje. */
public class MovimientoForm {

    @NotNull(message = "Elegí la bolsita")
    private Long bolsitaId;

    @NotNull
    private TipoMovimiento tipo = TipoMovimiento.DEPOSITO;

    @NotNull(message = "Poné la fecha")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @NotNull(message = "Poné el monto")
    @Positive(message = "El monto tiene que ser mayor a cero")
    private BigDecimal monto;

    /** Opcional, en porcentaje: si la cargás, actualiza la TNA de la bolsita. */
    @PositiveOrZero(message = "La tasa no puede ser negativa")
    @Max(value = 500, message = "Revisá la tasa: va en porcentaje (19 = 19%)")
    private BigDecimal tasaPct;

    @Size(max = 255)
    private String nota;

    public NuevoMovimiento aNuevoMovimiento() {
        return new NuevoMovimiento(bolsitaId, fecha, tipo, monto, Tasas.aFraccion(tasaPct), nota);
    }

    public Long getBolsitaId() { return bolsitaId; }
    public void setBolsitaId(Long bolsitaId) { this.bolsitaId = bolsitaId; }
    public TipoMovimiento getTipo() { return tipo; }
    public void setTipo(TipoMovimiento tipo) { this.tipo = tipo; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public BigDecimal getTasaPct() { return tasaPct; }
    public void setTasaPct(BigDecimal tasaPct) { this.tasaPct = tasaPct; }
    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }
}

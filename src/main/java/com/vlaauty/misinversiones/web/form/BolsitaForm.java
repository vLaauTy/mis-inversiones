package com.vlaauty.misinversiones.web.form;

import com.vlaauty.misinversiones.dominio.TipoBolsita;
import com.vlaauty.misinversiones.servicio.BolsitaService.DatosBolsita;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Formulario para crear o editar una bolsita. La tasa se carga en porcentaje. */
public class BolsitaForm {

    @NotBlank(message = "Poné un nombre")
    @Size(max = 80)
    private String nombre;

    @NotNull
    private TipoBolsita tipo = TipoBolsita.RESERVA;

    @Size(max = 80)
    private String entidad;

    @PositiveOrZero(message = "La tasa no puede ser negativa")
    @Max(value = 500, message = "Revisá la tasa: va en porcentaje (19 = 19%)")
    private BigDecimal tasaPct;

    @Positive(message = "La meta tiene que ser mayor a cero")
    private BigDecimal meta;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaUso;

    public DatosBolsita aDatos() {
        return new DatosBolsita(nombre, tipo, entidad, Tasas.aFraccion(tasaPct), meta, fechaUso);
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public TipoBolsita getTipo() { return tipo; }
    public void setTipo(TipoBolsita tipo) { this.tipo = tipo; }
    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }
    public BigDecimal getTasaPct() { return tasaPct; }
    public void setTasaPct(BigDecimal tasaPct) { this.tasaPct = tasaPct; }
    public BigDecimal getMeta() { return meta; }
    public void setMeta(BigDecimal meta) { this.meta = meta; }
    public LocalDate getFechaUso() { return fechaUso; }
    public void setFechaUso(LocalDate fechaUso) { this.fechaUso = fechaUso; }
}

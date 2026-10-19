package com.vlaauty.misinversiones.web.form;

import com.vlaauty.misinversiones.dominio.OrigenOperacion;
import com.vlaauty.misinversiones.dominio.TipoOperacion;
import com.vlaauty.misinversiones.servicio.OperacionService.NuevaOperacion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Formulario de compra o venta de una acción o CEDEAR. */
public class AccionForm {

    @NotNull(message = "Poné la fecha")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @NotNull(message = "Elegí el activo")
    private Long activoId;

    @NotNull
    private TipoOperacion tipo = TipoOperacion.COMPRA;

    @NotNull(message = "Poné la cantidad")
    @Positive(message = "La cantidad tiene que ser mayor a cero")
    private BigDecimal cantidad;

    @NotNull(message = "Poné el precio")
    @Positive(message = "El precio tiene que ser mayor a cero")
    private BigDecimal precioUnitario;

    /** Vacío = se estima con la comisión del broker. */
    @PositiveOrZero(message = "La comisión no puede ser negativa")
    private BigDecimal comision;

    @Size(max = 255)
    private String nota;

    public NuevaOperacion aNuevaOperacion() {
        return new NuevaOperacion(fecha, activoId, tipo, cantidad, precioUnitario, comision, OrigenOperacion.MANUAL, nota);
    }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public Long getActivoId() { return activoId; }
    public void setActivoId(Long activoId) { this.activoId = activoId; }
    public TipoOperacion getTipo() { return tipo; }
    public void setTipo(TipoOperacion tipo) { this.tipo = tipo; }
    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public BigDecimal getComision() { return comision; }
    public void setComision(BigDecimal comision) { this.comision = comision; }
    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }
}

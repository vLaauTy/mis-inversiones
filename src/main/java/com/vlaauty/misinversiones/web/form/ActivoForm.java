package com.vlaauty.misinversiones.web.form;

import com.vlaauty.misinversiones.dominio.NivelRiesgo;
import com.vlaauty.misinversiones.dominio.TipoActivo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Formulario para agregar un activo con su nivel de riesgo. */
public class ActivoForm {

    @NotBlank(message = "Poné el ticker")
    @Size(max = 20)
    @Pattern(regexp = "[A-Za-z0-9.]+", message = "El ticker solo lleva letras, números o punto")
    private String ticker;

    @NotBlank(message = "Poné el nombre")
    @Size(max = 120)
    private String nombre;

    @NotNull
    private TipoActivo tipo = TipoActivo.CEDEAR;

    @NotNull(message = "Elegí el nivel de riesgo")
    private NivelRiesgo nivelRiesgo;

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public TipoActivo getTipo() { return tipo; }
    public void setTipo(TipoActivo tipo) { this.tipo = tipo; }
    public NivelRiesgo getNivelRiesgo() { return nivelRiesgo; }
    public void setNivelRiesgo(NivelRiesgo nivelRiesgo) { this.nivelRiesgo = nivelRiesgo; }
}

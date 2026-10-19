package com.vlaauty.misinversiones.servicio;

import com.vlaauty.misinversiones.config.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Estima la comisión del broker: monto × porcentaje, más IVA sobre la comisión. */
@Component
public class CalculadoraComision {

    private final BigDecimal porcentaje;
    private final BigDecimal iva;

    @Autowired
    public CalculadoraComision(AppProperties propiedades) {
        this(propiedades.comision().porcentaje(), propiedades.comision().iva());
    }

    CalculadoraComision(BigDecimal porcentaje, BigDecimal iva) {
        this.porcentaje = porcentaje;
        this.iva = iva;
    }

    public BigDecimal estimar(BigDecimal cantidad, BigDecimal precioUnitario) {
        BigDecimal bruto = cantidad.multiply(precioUnitario);
        return bruto.multiply(porcentaje)
                .multiply(BigDecimal.ONE.add(iva))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public BigDecimal getIva() {
        return iva;
    }
}

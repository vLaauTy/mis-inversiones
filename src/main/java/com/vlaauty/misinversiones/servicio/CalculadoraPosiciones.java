package com.vlaauty.misinversiones.servicio;

import com.vlaauty.misinversiones.dominio.Activo;
import com.vlaauty.misinversiones.dominio.Operacion;
import com.vlaauty.misinversiones.dominio.TipoOperacion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Recorre las operaciones en orden cronológico y calcula, por activo, la cantidad, el costo
 * (costo promedio ponderado) y la ganancia ya realizada por ventas.
 */
public final class CalculadoraPosiciones {

    private static final MathContext PRECISION = MathContext.DECIMAL64;

    private CalculadoraPosiciones() {
    }

    /** @param operaciones ordenadas por fecha y luego por id */
    public static Map<Activo, EstadoPosicion> calcular(List<Operacion> operaciones) {
        Map<Activo, EstadoPosicion> estados = new LinkedHashMap<>();
        for (Operacion op : operaciones) {
            EstadoPosicion actual = estados.getOrDefault(op.getActivo(), EstadoPosicion.vacia());
            estados.put(op.getActivo(), aplicar(actual, op));
        }
        return estados;
    }

    static EstadoPosicion aplicar(EstadoPosicion estado, Operacion op) {
        BigDecimal bruto = op.getCantidad().multiply(op.getPrecioUnitario());
        if (op.getTipo() == TipoOperacion.COMPRA) {
            return new EstadoPosicion(
                    estado.cantidad().add(op.getCantidad()),
                    estado.costo().add(bruto).add(op.getComision()),
                    estado.gananciaRealizada(),
                    op.getPrecioUnitario());
        }
        // Venta: sale costo en proporción a la cantidad vendida.
        BigDecimal costoVendido = estado.cantidad().signum() == 0
                ? BigDecimal.ZERO
                : estado.costo().multiply(op.getCantidad()).divide(estado.cantidad(), PRECISION);
        BigDecimal neto = bruto.subtract(op.getComision());
        BigDecimal cantidadRestante = estado.cantidad().subtract(op.getCantidad());
        BigDecimal costoRestante = cantidadRestante.signum() <= 0
                ? BigDecimal.ZERO
                : estado.costo().subtract(costoVendido);
        return new EstadoPosicion(
                cantidadRestante.max(BigDecimal.ZERO),
                costoRestante,
                estado.gananciaRealizada().add(neto.subtract(costoVendido)),
                op.getPrecioUnitario());
    }
}

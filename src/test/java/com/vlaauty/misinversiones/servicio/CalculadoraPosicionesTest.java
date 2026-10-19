package com.vlaauty.misinversiones.servicio;

import com.vlaauty.misinversiones.dominio.Activo;
import com.vlaauty.misinversiones.dominio.NivelRiesgo;
import com.vlaauty.misinversiones.dominio.Operacion;
import com.vlaauty.misinversiones.dominio.OrigenOperacion;
import com.vlaauty.misinversiones.dominio.TipoActivo;
import com.vlaauty.misinversiones.dominio.TipoOperacion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraPosicionesTest {

    private final Activo spy = new Activo("SPY", "ETF S&P 500", TipoActivo.CEDEAR, NivelRiesgo.BAJO);
    private final Activo ko = new Activo("KO", "Coca-Cola", TipoActivo.CEDEAR, NivelRiesgo.BAJO);

    private Operacion op(Activo a, TipoOperacion tipo, String cantidad, String precio, String comision) {
        return new Operacion(LocalDate.of(2026, 10, 20), a, tipo, new BigDecimal(cantidad), new BigDecimal(precio),
                new BigDecimal(comision), OrigenOperacion.MANUAL, null);
    }

    @Test
    void comprasSumanCantidadYCostoConComision() {
        Map<Activo, EstadoPosicion> r = CalculadoraPosiciones.calcular(List.of(
                op(spy, TipoOperacion.COMPRA, "5", "20000", "544.50"),
                op(spy, TipoOperacion.COMPRA, "5", "22000", "598.95")));

        EstadoPosicion e = r.get(spy);
        assertThat(e.cantidad()).isEqualByComparingTo("10");
        assertThat(e.costo()).isEqualByComparingTo("211143.45");
        assertThat(e.costoPromedio()).isEqualByComparingTo("21114.3450");
    }

    @Test
    void ventaParcialSacaCostoPromedioYCalculaGanancia() {
        Map<Activo, EstadoPosicion> r = CalculadoraPosiciones.calcular(List.of(
                op(spy, TipoOperacion.COMPRA, "10", "20000", "0"),
                op(spy, TipoOperacion.VENTA, "4", "25000", "100")));

        EstadoPosicion e = r.get(spy);
        assertThat(e.cantidad()).isEqualByComparingTo("6");
        assertThat(e.costo()).isEqualByComparingTo("120000");
        // vende 4 × 25.000 − 100 = 99.900; costo de esas 4 = 80.000
        assertThat(e.gananciaRealizada()).isEqualByComparingTo("19900");
    }

    @Test
    void ventaTotalDejaPosicionEnCero() {
        Map<Activo, EstadoPosicion> r = CalculadoraPosiciones.calcular(List.of(
                op(ko, TipoOperacion.COMPRA, "3", "16000", "0"),
                op(ko, TipoOperacion.VENTA, "3", "15000", "0")));

        EstadoPosicion e = r.get(ko);
        assertThat(e.tieneTenencia()).isFalse();
        assertThat(e.costo()).isEqualByComparingTo("0");
        assertThat(e.gananciaRealizada()).isEqualByComparingTo("-3000");
    }

    @Test
    void separaActivos() {
        Map<Activo, EstadoPosicion> r = CalculadoraPosiciones.calcular(List.of(
                op(spy, TipoOperacion.COMPRA, "1", "20000", "0"),
                op(ko, TipoOperacion.COMPRA, "2", "16000", "0")));

        assertThat(r).hasSize(2);
        assertThat(r.get(ko).cantidad()).isEqualByComparingTo("2");
        assertThat(r.get(spy).ultimoPrecioOperado()).isEqualByComparingTo("20000");
    }
}

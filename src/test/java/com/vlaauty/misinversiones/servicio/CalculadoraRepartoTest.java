package com.vlaauty.misinversiones.servicio;

import com.vlaauty.misinversiones.dominio.Activo;
import com.vlaauty.misinversiones.dominio.NivelRiesgo;
import com.vlaauty.misinversiones.dominio.PlanInversion;
import com.vlaauty.misinversiones.dominio.TipoActivo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraRepartoTest {

    private final PlanInversion moderado = new PlanInversion("MODERADO", 50, 30, 20);

    private Posicion pos(String ticker, NivelRiesgo nivel, String valor) {
        Activo a = new Activo(ticker, ticker, TipoActivo.CEDEAR, nivel);
        return new Posicion(a, BigDecimal.ONE, new BigDecimal(valor), new BigDecimal(valor), null, null);
    }

    @Test
    void calculaPorcentajesYDesvios() {
        Reparto r = CalculadoraReparto.calcular(List.of(
                pos("SPY", NivelRiesgo.BAJO, "60000"),
                pos("QQQ", NivelRiesgo.MEDIO, "20000"),
                pos("NVDA", NivelRiesgo.ALTO, "20000")), moderado);

        assertThat(r.niveles()).extracting(Reparto.Nivel::pctRealEntero).containsExactly(60, 20, 20);
        assertThat(r.niveles()).extracting(Reparto.Nivel::desvioPuntos).containsExactly(10, -10, 0);
    }

    @Test
    void sugierePlataNuevaParaElNivelMasAtrasado() {
        Reparto r = CalculadoraReparto.calcular(List.of(
                pos("SPY", NivelRiesgo.BAJO, "60000"),
                pos("QQQ", NivelRiesgo.MEDIO, "20000"),
                pos("NVDA", NivelRiesgo.ALTO, "20000")), moderado);

        Reparto.Sugerencia s = r.sugerencia().orElseThrow();
        assertThat(s.nivel()).isEqualTo(NivelRiesgo.MEDIO);
        assertThat(s.puntosAbajo()).isEqualTo(10);
        // (0,30 × 100.000 − 20.000) / 0,70 = 14.286 → con eso medio queda en 30%
        assertThat(s.monto()).isEqualByComparingTo("14286");
    }

    @Test
    void sinDesvioNoSugiere() {
        Reparto r = CalculadoraReparto.calcular(List.of(
                pos("SPY", NivelRiesgo.BAJO, "50000"),
                pos("QQQ", NivelRiesgo.MEDIO, "30000"),
                pos("NVDA", NivelRiesgo.ALTO, "20000")), moderado);

        assertThat(r.sugerencia()).isEmpty();
    }

    @Test
    void carteraVaciaNoRompe() {
        Reparto r = CalculadoraReparto.calcular(List.of(), moderado);
        assertThat(r.niveles()).allMatch(n -> n.pctRealEntero() == 0);
        assertThat(r.sugerencia()).isEmpty();
    }
}

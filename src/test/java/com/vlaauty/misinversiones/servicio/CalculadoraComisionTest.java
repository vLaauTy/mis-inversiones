package com.vlaauty.misinversiones.servicio;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraComisionTest {

    private final CalculadoraComision cocos = new CalculadoraComision(new BigDecimal("0.0045"), new BigDecimal("0.21"));

    @Test
    void comisionCocosConIva() {
        // $200.000 × 0,45% = $900, + IVA = $1.089 (ejemplo del tarifario)
        assertThat(cocos.estimar(new BigDecimal("10"), new BigDecimal("20000"))).isEqualByComparingTo("1089.00");
    }

    @Test
    void redondeaADosDecimales() {
        // 2 × 19.800 = 39.600 → 178,2 + IVA = 215,622
        assertThat(cocos.estimar(new BigDecimal("2"), new BigDecimal("19800"))).isEqualByComparingTo("215.62");
    }
}

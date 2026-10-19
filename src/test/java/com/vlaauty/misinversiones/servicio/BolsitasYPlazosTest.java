package com.vlaauty.misinversiones.servicio;

import com.vlaauty.misinversiones.dominio.Bolsita;
import com.vlaauty.misinversiones.dominio.Instrumento;
import com.vlaauty.misinversiones.dominio.PlazoFijo;
import com.vlaauty.misinversiones.dominio.TipoBolsita;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BolsitasYPlazosTest {

    private final LocalDate hoy = LocalDate.of(2026, 10, 16);

    @Test
    void plazoFijoTradicionalEstimaInteres() {
        Bolsita enero = new Bolsita("Enero", TipoBolsita.PLAZO_FIJO, null, null, null, null);
        PlazoFijo pf = new PlazoFijo(enero, "Banco Provincia", Instrumento.PLAZO_FIJO, new BigDecimal("600000"),
                new BigDecimal("0.22"), hoy, hoy.plusDays(30));

        // 600.000 × 22% × 30 / 365 = 10.849,32
        assertThat(pf.getInteresEstimado()).hasValueSatisfying(i -> assertThat(i).isEqualByComparingTo("10849.32"));
        assertThat(pf.getTotalAlVencer()).hasValueSatisfying(t -> assertThat(t).isEqualByComparingTo("610849.32"));
        assertThat(pf.diasParaVencer(hoy)).isEqualTo(30);
    }

    @Test
    void plazoFijoUvaNoEstima() {
        Bolsita enero = new Bolsita("Enero", TipoBolsita.PLAZO_FIJO, null, null, null, null);
        PlazoFijo uva = new PlazoFijo(enero, "Banco Nación", Instrumento.PLAZO_FIJO_UVA, new BigDecimal("100000"),
                new BigDecimal("0.01"), hoy, hoy.plusDays(90));
        assertThat(uva.getInteresEstimado()).isEmpty();
    }

    @Test
    void reservaBlackFriday() {
        Bolsita bf = new Bolsita("Black Friday", TipoBolsita.RESERVA, "Mercado Pago", new BigDecimal("0.19"),
                new BigDecimal("500000"), LocalDate.of(2026, 11, 27));
        ResumenBolsita r = new ResumenBolsita(bf, new BigDecimal("500000"), List.of(), hoy);

        assertThat(r.rindePorDia()).isEqualByComparingTo("260.27");
        assertThat(r.diasParaUso()).isEqualTo(42L);
        assertThat(r.estimadoHastaUso()).isEqualByComparingTo("10931.34");
        assertThat(r.avanceMeta()).isEqualTo(100);
    }

    @Test
    void saldoIncluyePlazosActivos() {
        Bolsita enero = new Bolsita("Enero", TipoBolsita.PLAZO_FIJO, null, null, null, null);
        PlazoFijo pf = new PlazoFijo(enero, "Banco Provincia", Instrumento.PLAZO_FIJO, new BigDecimal("600000"),
                new BigDecimal("0.22"), hoy, hoy.plusDays(30));
        ResumenBolsita r = new ResumenBolsita(enero, new BigDecimal("50000"), List.of(pf), hoy);

        assertThat(r.saldo()).isEqualByComparingTo("650000");
        assertThat(r.avanceMeta()).isNull();
    }
}

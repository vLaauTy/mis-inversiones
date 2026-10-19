package com.vlaauty.misinversiones.web.form;

import java.math.BigDecimal;

/** Los formularios piden la tasa en porcentaje (22) y se guarda como fracción (0.22). */
final class Tasas {

    private Tasas() {
    }

    static BigDecimal aFraccion(BigDecimal porcentaje) {
        return porcentaje == null ? null : porcentaje.movePointLeft(2);
    }
}

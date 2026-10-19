package com.vlaauty.misinversiones.servicio;

import com.vlaauty.misinversiones.dominio.PlanInversion;
import com.vlaauty.misinversiones.dominio.PlazoFijo;
import com.vlaauty.misinversiones.repositorio.PlanInversionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** Arma todo lo que muestra la pantalla de Inicio. */
@Service
@Transactional(readOnly = true)
public class InicioService {

    private final CarteraService cartera;
    private final BolsitaService bolsitas;
    private final PlanInversionRepository planes;

    public InicioService(CarteraService cartera, BolsitaService bolsitas, PlanInversionRepository planes) {
        this.cartera = cartera;
        this.bolsitas = bolsitas;
        this.planes = planes;
    }

    public PlanInversion plan() {
        return planes.findFirstByOrderByIdAsc().orElseGet(() -> new PlanInversion("MODERADO", 50, 30, 20));
    }

    public Resumen resumen() {
        List<Posicion> posiciones = cartera.posiciones();
        TotalesCartera totales = TotalesCartera.de(posiciones);
        List<ResumenBolsita> resumenes = bolsitas.resumenes();
        BigDecimal enBolsitas = resumenes.stream().map(ResumenBolsita::saldo).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Resumen(
                enBolsitas.add(totales.valor()),
                totales,
                resumenes,
                CalculadoraReparto.calcular(posiciones, plan()),
                bolsitas.proximosVencimientos(7),
                !posiciones.isEmpty());
    }

    public record Resumen(BigDecimal patrimonio, TotalesCartera acciones, List<ResumenBolsita> bolsitas,
                          Reparto reparto, List<PlazoFijo> vencimientos, boolean tieneAcciones) {
    }
}

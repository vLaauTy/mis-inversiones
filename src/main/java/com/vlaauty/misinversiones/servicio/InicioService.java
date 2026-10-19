package com.vlaauty.misinversiones.servicio;

import com.vlaauty.misinversiones.dominio.DolarCcl;
import com.vlaauty.misinversiones.dominio.PlanInversion;
import com.vlaauty.misinversiones.dominio.PlazoFijo;
import com.vlaauty.misinversiones.repositorio.DolarCclRepository;
import com.vlaauty.misinversiones.repositorio.PlanInversionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/** Arma todo lo que muestra la pantalla de Inicio. */
@Service
@Transactional(readOnly = true)
public class InicioService {

    private final CarteraService cartera;
    private final BolsitaService bolsitas;
    private final PlanInversionRepository planes;
    private final DolarCclRepository dolares;

    public InicioService(CarteraService cartera, BolsitaService bolsitas, PlanInversionRepository planes,
                         DolarCclRepository dolares) {
        this.cartera = cartera;
        this.bolsitas = bolsitas;
        this.planes = planes;
        this.dolares = dolares;
    }

    public PlanInversion plan() {
        return planes.findFirstByOrderByIdAsc().orElseGet(() -> new PlanInversion("MODERADO", 50, 30, 20));
    }

    public Resumen resumen() {
        List<Posicion> posiciones = cartera.posiciones();
        TotalesCartera totales = TotalesCartera.de(posiciones);
        List<ResumenBolsita> resumenes = bolsitas.resumenes();
        BigDecimal enBolsitas = resumenes.stream().map(ResumenBolsita::saldo).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal patrimonio = enBolsitas.add(totales.valor());
        Optional<DolarCcl> ccl = dolares.findFirstByOrderByFechaHoraDescIdDesc();
        return new Resumen(
                patrimonio,
                ccl.orElse(null),
                ccl.map(d -> patrimonio.divide(d.getVenta(), 2, RoundingMode.HALF_UP)).orElse(null),
                totales,
                resumenes,
                CalculadoraReparto.calcular(posiciones, plan()),
                bolsitas.proximosVencimientos(7),
                !posiciones.isEmpty());
    }

    /**
     * @param ccl            última cotización guardada del dólar CCL; null hasta la primera actualización
     * @param patrimonioUsd  patrimonio pasado a dólares con el CCL de venta; null si no hay cotización
     */
    public record Resumen(BigDecimal patrimonio, DolarCcl ccl, BigDecimal patrimonioUsd,
                          TotalesCartera acciones, List<ResumenBolsita> bolsitas,
                          Reparto reparto, List<PlazoFijo> vencimientos, boolean tieneAcciones) {
    }
}

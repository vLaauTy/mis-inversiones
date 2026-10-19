package com.vlaauty.misinversiones.servicio;

import com.vlaauty.misinversiones.dominio.Activo;
import com.vlaauty.misinversiones.dominio.FuentePrecio;
import com.vlaauty.misinversiones.dominio.Precio;
import com.vlaauty.misinversiones.repositorio.OperacionRepository;
import com.vlaauty.misinversiones.repositorio.PrecioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Arma la cartera a partir de las operaciones y valúa cada posición con su último precio. */
@Service
@Transactional(readOnly = true)
public class CarteraService {

    private static final Comparator<Posicion> POR_RIESGO_Y_TICKER = Comparator
            .comparing((Posicion p) -> p.activo().getNivelRiesgo())
            .thenComparing(p -> p.activo().getTicker());

    private final OperacionRepository operaciones;
    private final PrecioRepository precios;
    private final Clock reloj;

    public CarteraService(OperacionRepository operaciones, PrecioRepository precios, Clock reloj) {
        this.operaciones = operaciones;
        this.precios = precios;
        this.reloj = reloj;
    }

    /** Posiciones con tenencia, ordenadas por nivel de riesgo y ticker. */
    public List<Posicion> posiciones() {
        return estados().entrySet().stream()
                .filter(e -> e.getValue().tieneTenencia())
                .map(e -> valuar(e.getKey(), e.getValue()))
                .sorted(POR_RIESGO_Y_TICKER)
                .toList();
    }

    public BigDecimal cantidadDe(Activo activo) {
        EstadoPosicion estado = CalculadoraPosiciones.calcular(operaciones.findByActivoCronologico(activo)).get(activo);
        return estado == null ? BigDecimal.ZERO : estado.cantidad();
    }

    public BigDecimal gananciaRealizada() {
        return estados().values().stream()
                .map(EstadoPosicion::gananciaRealizada)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void registrarPrecioManual(Activo activo, BigDecimal precioArs) {
        if (precioArs == null || precioArs.signum() <= 0) {
            throw new ReglaNegocioException("El precio tiene que ser mayor a cero.");
        }
        precios.save(new Precio(activo, LocalDateTime.now(reloj), precioArs, FuentePrecio.MANUAL));
    }

    private Map<Activo, EstadoPosicion> estados() {
        return CalculadoraPosiciones.calcular(operaciones.findAllCronologico());
    }

    /** Usa el último precio cargado; si no hay ninguno, el de la última operación. */
    private Posicion valuar(Activo activo, EstadoPosicion estado) {
        Optional<Precio> ultimo = precios.findFirstByActivoOrderByFechaHoraDescIdDesc(activo);
        return new Posicion(
                activo,
                estado.cantidad(),
                estado.costo(),
                ultimo.map(Precio::getPrecioArs).orElse(estado.ultimoPrecioOperado()),
                ultimo.map(Precio::getFuente).orElse(null),
                ultimo.map(Precio::getFechaHora).orElse(null));
    }
}

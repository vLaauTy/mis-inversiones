package com.vlaauty.misinversiones.servicio;

import com.vlaauty.misinversiones.dominio.Bolsita;
import com.vlaauty.misinversiones.dominio.EstadoPlazoFijo;
import com.vlaauty.misinversiones.dominio.Instrumento;
import com.vlaauty.misinversiones.dominio.MovimientoBolsita;
import com.vlaauty.misinversiones.dominio.PlazoFijo;
import com.vlaauty.misinversiones.dominio.TipoBolsita;
import com.vlaauty.misinversiones.dominio.TipoMovimiento;
import com.vlaauty.misinversiones.repositorio.BolsitaRepository;
import com.vlaauty.misinversiones.repositorio.MovimientoBolsitaRepository;
import com.vlaauty.misinversiones.repositorio.PlazoFijoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** Bolsitas, sus movimientos (depósitos, retiros, intereses) y los plazos fijos que contienen. */
@Service
@Transactional(readOnly = true)
public class BolsitaService {

    private final BolsitaRepository bolsitas;
    private final MovimientoBolsitaRepository movimientos;
    private final PlazoFijoRepository plazos;
    private final Clock reloj;

    public BolsitaService(BolsitaRepository bolsitas, MovimientoBolsitaRepository movimientos,
                          PlazoFijoRepository plazos, Clock reloj) {
        this.bolsitas = bolsitas;
        this.movimientos = movimientos;
        this.plazos = plazos;
        this.reloj = reloj;
    }

    public List<Bolsita> todas() {
        return bolsitas.findAllByOrderByIdAsc();
    }

    public Bolsita buscar(Long id) {
        return bolsitas.findById(id).orElseThrow(() -> new ReglaNegocioException("Esa bolsita no existe."));
    }

    public List<ResumenBolsita> resumenes() {
        LocalDate hoy = hoy();
        return todas().stream().map(b -> resumen(b, hoy)).toList();
    }

    @Transactional
    public Bolsita crear(DatosBolsita datos) {
        if (bolsitas.existsByNombreIgnoreCase(datos.nombre().trim())) {
            throw new ReglaNegocioException("Ya existe una bolsita llamada " + datos.nombre().trim() + ".");
        }
        return bolsitas.save(new Bolsita(datos.nombre(), datos.tipo(), Textos.vacioANull(datos.entidad()),
                datos.tasa(), datos.meta(), datos.fechaUso()));
    }

    @Transactional
    public void actualizar(Long id, DatosBolsita datos) {
        buscar(id).actualizar(datos.nombre(), datos.tipo(), Textos.vacioANull(datos.entidad()), datos.tasa(),
                datos.meta(), datos.fechaUso());
    }

    /**
     * Registra un depósito, retiro o interés. Si se informa una tasa, actualiza la TNA actual de la bolsita.
     *
     * @throws ReglaNegocioException si se quiere retirar más de lo disponible fuera de plazos fijos
     */
    @Transactional
    public MovimientoBolsita registrarMovimiento(NuevoMovimiento nuevo) {
        Bolsita bolsita = buscar(nuevo.bolsitaId());
        if (nuevo.tipo() == TipoMovimiento.RETIRO
                && nuevo.monto().compareTo(resumen(bolsita, hoy()).saldoMovimientos()) > 0) {
            throw new ReglaNegocioException("No podés retirar más de lo que hay en " + bolsita.getNombre()
                    + " fuera de plazos fijos.");
        }
        if (nuevo.tasaActual() != null) {
            bolsita.setTasa(nuevo.tasaActual());
        }
        return movimientos.save(new MovimientoBolsita(bolsita, nuevo.fecha(), nuevo.tipo(), nuevo.monto(),
                Textos.vacioANull(nuevo.nota())));
    }

    @Transactional
    public PlazoFijo crearPlazoFijo(NuevoPlazoFijo nuevo) {
        if (nuevo.plazoDias() <= 0) {
            throw new ReglaNegocioException("El plazo tiene que ser de al menos un día.");
        }
        return plazos.save(new PlazoFijo(buscar(nuevo.bolsitaId()), nuevo.entidad(), nuevo.instrumento(),
                nuevo.monto(), nuevo.tasa(), nuevo.fechaInicio(), nuevo.fechaVencimiento()));
    }

    /**
     * Marca un plazo fijo como cobrado: deja de contar como activo y lo cobrado entra como depósito
     * en la misma bolsita, disponible para renovar o retirar. Sin monto, se toma el total estimado.
     */
    @Transactional
    public void cobrarPlazoFijo(Long plazoId, BigDecimal montoCobrado) {
        PlazoFijo plazo = plazos.findById(plazoId)
                .orElseThrow(() -> new ReglaNegocioException("Ese plazo fijo no existe."));
        if (!plazo.isActivo()) {
            throw new ReglaNegocioException("Ese plazo fijo ya estaba cobrado.");
        }
        BigDecimal cobrado = montoCobrado != null ? montoCobrado : plazo.getTotalAlVencer().orElse(plazo.getMonto());
        plazo.marcarCobrado(cobrado);
        movimientos.save(new MovimientoBolsita(plazo.getBolsita(), hoy(), TipoMovimiento.DEPOSITO, cobrado,
                "Cobro " + plazo.getInstrumento().getEtiqueta() + " " + plazo.getEntidad()));
    }

    public List<PlazoFijo> todosLosPlazos() {
        return plazos.findAllByOrderByFechaVencimientoDesc();
    }

    public List<MovimientoBolsita> historialMovimientos() {
        return movimientos.findAllByOrderByFechaDescIdDesc();
    }

    /** Plazos activos que vencen dentro de los próximos días, incluidos los vencidos sin cobrar. */
    public List<PlazoFijo> proximosVencimientos(int dias) {
        LocalDate limite = hoy().plusDays(dias);
        return plazos.findByEstadoOrderByFechaVencimientoAsc(EstadoPlazoFijo.ACTIVO).stream()
                .filter(p -> !p.getFechaVencimiento().isAfter(limite))
                .toList();
    }

    private ResumenBolsita resumen(Bolsita bolsita, LocalDate hoy) {
        BigDecimal saldo = movimientos.findByBolsita(bolsita).stream()
                .map(MovimientoBolsita::getMontoConSigno)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ResumenBolsita(bolsita, saldo, plazos.findByBolsitaAndEstado(bolsita, EstadoPlazoFijo.ACTIVO), hoy);
    }

    private LocalDate hoy() {
        return LocalDate.now(reloj);
    }

    /** @param tasa TNA como fracción, o null */
    public record DatosBolsita(String nombre, TipoBolsita tipo, String entidad, BigDecimal tasa, BigDecimal meta,
                               LocalDate fechaUso) {
    }

    /** @param tasaActual TNA como fracción, o null para no cambiarla */
    public record NuevoMovimiento(Long bolsitaId, LocalDate fecha, TipoMovimiento tipo, BigDecimal monto,
                                  BigDecimal tasaActual, String nota) {
    }

    /** @param tasa TNA como fracción */
    public record NuevoPlazoFijo(Long bolsitaId, String entidad, Instrumento instrumento, BigDecimal monto,
                                 BigDecimal tasa, LocalDate fechaInicio, int plazoDias) {

        public LocalDate fechaVencimiento() {
            return fechaInicio.plusDays(plazoDias);
        }
    }
}

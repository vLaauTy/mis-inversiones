package com.vlaauty.misinversiones.servicio;

import com.vlaauty.misinversiones.dominio.Activo;
import com.vlaauty.misinversiones.dominio.Operacion;
import com.vlaauty.misinversiones.dominio.OrigenOperacion;
import com.vlaauty.misinversiones.dominio.TipoOperacion;
import com.vlaauty.misinversiones.repositorio.OperacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Alta y baja de compras y ventas, con las reglas que mantienen la cartera consistente. */
@Service
@Transactional(readOnly = true)
public class OperacionService {

    private final OperacionRepository operaciones;
    private final ActivoService activos;
    private final CarteraService cartera;
    private final CalculadoraComision comisiones;

    public OperacionService(OperacionRepository operaciones, ActivoService activos, CarteraService cartera,
                            CalculadoraComision comisiones) {
        this.operaciones = operaciones;
        this.activos = activos;
        this.cartera = cartera;
        this.comisiones = comisiones;
    }

    /**
     * Registra una compra o venta. Si no se informa la comisión, se estima con la del broker.
     *
     * @throws ReglaNegocioException si es una venta por más de lo que tenés
     */
    @Transactional
    public Operacion registrar(NuevaOperacion nueva) {
        Activo activo = activos.buscar(nueva.activoId());
        if (nueva.tipo() == TipoOperacion.VENTA) {
            validarTenenciaSuficiente(activo, nueva.cantidad());
        }
        BigDecimal comision = nueva.comision() != null
                ? nueva.comision()
                : comisiones.estimar(nueva.cantidad(), nueva.precioUnitario());
        return operaciones.save(new Operacion(nueva.fecha(), activo, nueva.tipo(), nueva.cantidad(),
                nueva.precioUnitario(), comision, nueva.origen(), Textos.vacioANull(nueva.nota())));
    }

    public List<Operacion> historial() {
        return operaciones.findAllRecientesPrimero();
    }

    /**
     * Borra una operación cargada por error.
     *
     * @throws ReglaNegocioException si borrarla deja ventas sin las compras que las respaldan
     */
    @Transactional
    public void borrar(Long id) {
        Operacion operacion = operaciones.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("Esa operación no existe."));
        Activo activo = operacion.getActivo();
        operaciones.delete(operacion);
        operaciones.flush();
        if (!tenenciaNuncaNegativa(activo)) {
            throw new ReglaNegocioException("No se puede borrar: dejaría ventas de " + activo.getTicker()
                    + " sin las compras que las respaldan. Borrá primero la venta.");
        }
    }

    private void validarTenenciaSuficiente(Activo activo, BigDecimal cantidad) {
        BigDecimal tenencia = cartera.cantidadDe(activo);
        if (cantidad.compareTo(tenencia) > 0) {
            throw new ReglaNegocioException("Querés vender " + plano(cantidad) + " " + activo.getTicker()
                    + " pero tenés " + plano(tenencia) + ".");
        }
    }

    /** Recorre las operaciones del activo en orden y verifica que nunca se venda más de lo que había. */
    private boolean tenenciaNuncaNegativa(Activo activo) {
        BigDecimal tenencia = BigDecimal.ZERO;
        for (Operacion o : operaciones.findByActivoCronologico(activo)) {
            tenencia = o.getTipo() == TipoOperacion.COMPRA
                    ? tenencia.add(o.getCantidad())
                    : tenencia.subtract(o.getCantidad());
            if (tenencia.signum() < 0) {
                return false;
            }
        }
        return true;
    }

    private static String plano(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString();
    }

    /** Datos para registrar una operación. */
    public record NuevaOperacion(LocalDate fecha, Long activoId, TipoOperacion tipo, BigDecimal cantidad,
                                 BigDecimal precioUnitario, BigDecimal comision, OrigenOperacion origen,
                                 String nota) {
    }
}

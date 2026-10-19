package com.vlaauty.misinversiones.servicio;

import com.vlaauty.misinversiones.config.PreciosProperties;
import com.vlaauty.misinversiones.dominio.Activo;
import com.vlaauty.misinversiones.dominio.DolarCcl;
import com.vlaauty.misinversiones.dominio.FuentePrecio;
import com.vlaauty.misinversiones.dominio.Precio;
import com.vlaauty.misinversiones.dominio.TipoActivo;
import com.vlaauty.misinversiones.integracion.Cotizacion;
import com.vlaauty.misinversiones.integracion.DolarApiCliente;
import com.vlaauty.misinversiones.integracion.ProveedorCotizaciones;
import com.vlaauty.misinversiones.integracion.ProveedorNoDisponibleException;
import com.vlaauty.misinversiones.repositorio.ActivoRepository;
import com.vlaauty.misinversiones.repositorio.DolarCclRepository;
import com.vlaauty.misinversiones.repositorio.PrecioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Trae precios de las fuentes externas en el orden configurado y los guarda.
 * Si una fuente falla o no tiene un ticker, ese ticker se busca en la siguiente.
 *
 * <p>No es transaccional a propósito: no conviene tener una transacción abierta mientras se espera la red.
 */
@Service
public class ActualizadorPrecios {

    private static final Logger log = LoggerFactory.getLogger(ActualizadorPrecios.class);

    private final List<ProveedorCotizaciones> proveedores;
    private final DolarApiCliente dolarApi;
    private final ActivoRepository activos;
    private final PrecioRepository precios;
    private final DolarCclRepository dolares;
    private final Clock reloj;

    public ActualizadorPrecios(List<ProveedorCotizaciones> disponibles, PreciosProperties propiedades,
                               DolarApiCliente dolarApi, ActivoRepository activos, PrecioRepository precios,
                               DolarCclRepository dolares, Clock reloj) {
        this.proveedores = enOrden(disponibles, propiedades.fuentes());
        this.dolarApi = dolarApi;
        this.activos = activos;
        this.precios = precios;
        this.dolares = dolares;
        this.reloj = reloj;
    }

    /** Ordena las fuentes como dice la configuración y descarta las que no figuran. */
    private static List<ProveedorCotizaciones> enOrden(List<ProveedorCotizaciones> disponibles, List<FuentePrecio> orden) {
        return disponibles.stream()
                .filter(p -> orden.contains(p.fuente()))
                .sorted(Comparator.comparingInt(p -> orden.indexOf(p.fuente())))
                .toList();
    }

    public ResultadoActualizacion actualizar() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        Map<FuentePrecio, Integer> porFuente = new EnumMap<>(FuentePrecio.class);
        List<String> sinPrecio = new ArrayList<>();

        Map<TipoActivo, List<Activo>> porTipo = activos.findAll().stream()
                .collect(Collectors.groupingBy(Activo::getTipo));
        porTipo.forEach((tipo, delTipo) -> {
            Map<String, Activo> pendientes = delTipo.stream()
                    .collect(Collectors.toMap(Activo::getTicker, Function.identity(), (a, b) -> a, LinkedHashMap::new));
            for (ProveedorCotizaciones proveedor : proveedores) {
                if (pendientes.isEmpty()) {
                    break;
                }
                Map<String, Cotizacion> cotizaciones = consultar(proveedor, tipo);
                for (Iterator<Activo> it = pendientes.values().iterator(); it.hasNext(); ) {
                    Activo activo = it.next();
                    Cotizacion c = cotizaciones.get(activo.getTicker());
                    if (c != null) {
                        guardar(activo, c, proveedor.fuente(), ahora);
                        porFuente.merge(proveedor.fuente(), 1, Integer::sum);
                        it.remove();
                    }
                }
            }
            sinPrecio.addAll(pendientes.keySet());
        });

        boolean dolarActualizado = actualizarDolar(ahora);
        ResultadoActualizacion resultado = new ResultadoActualizacion(porFuente, sinPrecio, dolarActualizado);
        log.info("Precios actualizados: {}", resultado);
        return resultado;
    }

    private Map<String, Cotizacion> consultar(ProveedorCotizaciones proveedor, TipoActivo tipo) {
        try {
            return proveedor.cotizaciones(tipo);
        } catch (ProveedorNoDisponibleException e) {
            log.warn("{} ({}): {}", proveedor.fuente(), tipo, e.getMessage());
            return Map.of();
        }
    }

    /** Fuera del horario de mercado la fuente repite el cierre: no hace falta guardarlo de nuevo. */
    private void guardar(Activo activo, Cotizacion c, FuentePrecio fuente, LocalDateTime ahora) {
        Optional<Precio> ultimo = precios.findFirstByActivoOrderByFechaHoraDescIdDesc(activo);
        if (ultimo.isPresent() && ultimo.get().repite(c.precio(), fuente, ahora)) {
            return;
        }
        precios.save(new Precio(activo, ahora, c.precio(), fuente, c.variacionDiaria()));
    }

    private boolean actualizarDolar(LocalDateTime ahora) {
        try {
            DolarApiCliente.CotizacionDolar ccl = dolarApi.ccl();
            dolares.save(new DolarCcl(ahora, ccl.compra(), ccl.venta()));
            return true;
        } catch (ProveedorNoDisponibleException e) {
            log.warn(e.getMessage());
            return false;
        }
    }
}

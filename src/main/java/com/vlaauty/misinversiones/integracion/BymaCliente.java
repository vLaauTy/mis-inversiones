package com.vlaauty.misinversiones.integracion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vlaauty.misinversiones.config.PreciosProperties;
import com.vlaauty.misinversiones.dominio.FuentePrecio;
import com.vlaauty.misinversiones.dominio.TipoActivo;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Precios de BYMA Open Data (open.bymadata.com.ar), con unos 20 minutos de demora.
 * No es una API documentada: los paneles se piden por POST y pueden cambiar sin aviso,
 * por eso siempre hay otra fuente detrás.
 */
@Component
public class BymaCliente implements ProveedorCotizaciones {

    /** Mismo cuerpo que manda la web de BYMA: todos los plazos, sin especies sin precio. */
    private static final Map<String, Object> FILTRO = Map.of(
            "excludeZeroPxAndQty", true, "T0", true, "T1", true, "T2", true);

    /** Plazo preferido: 24 hs es el que muestra Cocos por defecto. Después contado inmediato. */
    private static final List<String> PLAZOS_PREFERIDOS = List.of("2", "1", "3");

    private final RestClient http;

    public BymaCliente(RestClient.Builder builder, PreciosProperties propiedades) {
        this.http = builder.baseUrl(propiedades.bymaUrl())
                .defaultHeader("Referer", "https://open.bymadata.com.ar/")
                .build();
    }

    @Override
    public FuentePrecio fuente() {
        return FuentePrecio.BYMA;
    }

    @Override
    public Map<String, Cotizacion> cotizaciones(TipoActivo tipo) {
        try {
            List<Item> items = switch (tipo) {
                case ACCION -> Stream.concat(pagina("/leading-equity").stream(), pagina("/general-equity").stream()).toList();
                case CEDEAR -> lista("/cedears");
            };
            return elegirPorTicker(items);
        } catch (RestClientException e) {
            throw new ProveedorNoDisponibleException("BYMA", e);
        }
    }

    /** Paneles que responden {"data":[...]} (acciones líderes y panel general). */
    private List<Item> pagina(String panel) {
        Pagina pagina = http.post().uri(panel).contentType(MediaType.APPLICATION_JSON).body(FILTRO)
                .retrieve().body(Pagina.class);
        return pagina == null || pagina.data() == null ? List.of() : pagina.data();
    }

    /** Paneles que responden la lista directamente (CEDEARs). */
    private List<Item> lista(String panel) {
        List<Item> items = http.post().uri(panel).contentType(MediaType.APPLICATION_JSON).body(FILTRO)
                .retrieve().body(new ParameterizedTypeReference<>() {
                });
        return items == null ? List.of() : items;
    }

    /** Se queda con una fila en pesos por ticker, del plazo preferido. */
    static Map<String, Cotizacion> elegirPorTicker(List<Item> items) {
        Map<String, Item> elegidos = new HashMap<>();
        items.stream()
                .filter(Item::enPesosConPrecio)
                .sorted(Comparator.comparingInt(Item::prioridadPlazo))
                .forEach(i -> elegidos.putIfAbsent(i.ticker(), i));
        Map<String, Cotizacion> cotizaciones = new HashMap<>();
        elegidos.forEach((ticker, i) -> cotizaciones.put(ticker, new Cotizacion(ticker, i.precio(), i.imbalance())));
        return cotizaciones;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Pagina(List<Item> data) {
    }

    /**
     * Una fila del panel. {@code imbalance} es la variación contra el cierre anterior, ya en fracción.
     * {@code settlementType}: "1" contado inmediato, "2" 24 hs, "3" 48 hs.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Item(String symbol, String settlementType, String denominationCcy,
                BigDecimal trade, BigDecimal closingPrice, BigDecimal previousClosingPrice, BigDecimal imbalance) {

        String ticker() {
            return symbol.trim().toUpperCase();
        }

        /** Último operado; si hoy no operó, el cierre; si tampoco, el cierre anterior. */
        BigDecimal precio() {
            return Stream.of(trade, closingPrice, previousClosingPrice)
                    .filter(Objects::nonNull)
                    .filter(p -> p.signum() > 0)
                    .findFirst()
                    .orElse(null);
        }

        boolean enPesosConPrecio() {
            return symbol != null && "ARS".equalsIgnoreCase(denominationCcy) && precio() != null;
        }

        int prioridadPlazo() {
            int i = PLAZOS_PREFERIDOS.indexOf(settlementType);
            return i < 0 ? PLAZOS_PREFERIDOS.size() : i;
        }
    }
}

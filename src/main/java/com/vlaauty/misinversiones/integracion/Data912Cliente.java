package com.vlaauty.misinversiones.integracion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vlaauty.misinversiones.config.PreciosProperties;
import com.vlaauty.misinversiones.dominio.FuentePrecio;
import com.vlaauty.misinversiones.dominio.TipoActivo;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Precios de data912.com: un GET por panel, sin autenticación.
 * Ejemplo de respuesta: {@code [{"symbol":"ALUA","px_bid":832.0,"px_ask":835.0,"c":833.0,"pct_change":-1.42}]}
 */
@Component
public class Data912Cliente implements ProveedorCotizaciones {

    private final RestClient http;

    public Data912Cliente(RestClient.Builder builder, PreciosProperties propiedades) {
        this.http = builder.baseUrl(propiedades.data912Url()).build();
    }

    @Override
    public FuentePrecio fuente() {
        return FuentePrecio.DATA912;
    }

    @Override
    public Map<String, Cotizacion> cotizaciones(TipoActivo tipo) {
        String panel = switch (tipo) {
            case ACCION -> "/live/arg_stocks";
            case CEDEAR -> "/live/arg_cedears";
        };
        try {
            List<Item> items = http.get().uri(panel).retrieve().body(new ParameterizedTypeReference<>() {
            });
            return items == null ? Map.of() : items.stream()
                    .filter(Item::tienePrecio)
                    .map(Item::aCotizacion)
                    .collect(Collectors.toMap(Cotizacion::ticker, Function.identity(), (a, b) -> a));
        } catch (RestClientException e) {
            throw new ProveedorNoDisponibleException("data912", e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Item(String symbol, BigDecimal c, @JsonProperty("pct_change") BigDecimal pctChange) {

        boolean tienePrecio() {
            return symbol != null && c != null && c.signum() > 0;
        }

        Cotizacion aCotizacion() {
            BigDecimal variacion = pctChange == null ? null : pctChange.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
            return new Cotizacion(symbol.trim().toUpperCase(), c, variacion);
        }
    }
}

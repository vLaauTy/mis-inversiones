package com.vlaauty.misinversiones.integracion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vlaauty.misinversiones.config.PreciosProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;

/**
 * Dólar contado con liquidación de dolarapi.com.
 * Respuesta: {@code {"casa":"contadoconliqui","compra":1623.6,"venta":1625.6,"fechaActualizacion":"..."}}
 */
@Component
public class DolarApiCliente {

    private final RestClient http;

    public DolarApiCliente(RestClient.Builder builder, PreciosProperties propiedades) {
        this.http = builder.baseUrl(propiedades.dolarUrl()).build();
    }

    public CotizacionDolar ccl() {
        try {
            CotizacionDolar cotizacion = http.get().uri("/v1/dolares/contadoconliqui").retrieve().body(CotizacionDolar.class);
            if (cotizacion == null || !cotizacion.valida()) {
                throw new IllegalStateException("respuesta sin compra/venta");
            }
            return cotizacion;
        } catch (RestClientException | IllegalStateException e) {
            throw new ProveedorNoDisponibleException("dolarapi", e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CotizacionDolar(BigDecimal compra, BigDecimal venta) {

        boolean valida() {
            return compra != null && venta != null && compra.signum() > 0 && venta.signum() > 0;
        }
    }
}

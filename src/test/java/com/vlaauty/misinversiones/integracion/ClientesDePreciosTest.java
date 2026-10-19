package com.vlaauty.misinversiones.integracion;

import com.vlaauty.misinversiones.config.PreciosProperties;
import com.vlaauty.misinversiones.dominio.FuentePrecio;
import com.vlaauty.misinversiones.dominio.TipoActivo;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Las respuestas de ejemplo son recortes de respuestas reales de cada servicio. */
class ClientesDePreciosTest {

    private static final PreciosProperties PROPIEDADES = new PreciosProperties(false, "0 0 * * * *",
            List.of(FuentePrecio.BYMA, FuentePrecio.DATA912), "https://byma.test/free", "https://data912.test",
            "https://dolar.test");

    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();

    @Test
    void data912LeeElPanelYPasaLaVariacionAFraccion() {
        servidor.expect(requestTo("https://data912.test/live/arg_cedears")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [{"symbol":"SPY","q_bid":10.0,"px_bid":44900.0,"px_ask":45100.0,"q_ask":3.0,"v":1.0E7,
                          "q_op":300.0,"c":45000.0,"pct_change":1.25},
                         {"symbol":"VIEJO","c":0.0,"pct_change":0.0}]
                        """, MediaType.APPLICATION_JSON));

        Map<String, Cotizacion> cotizaciones = new Data912Cliente(builder, PROPIEDADES).cotizaciones(TipoActivo.CEDEAR);

        assertThat(cotizaciones).containsOnlyKeys("SPY");
        assertThat(cotizaciones.get("SPY").precio()).isEqualByComparingTo("45000");
        assertThat(cotizaciones.get("SPY").variacionDiaria()).isEqualByComparingTo("0.0125");
        servidor.verify();
    }

    @Test
    void data912CaidoSeInformaComoProveedorNoDisponible() {
        servidor.expect(requestTo("https://data912.test/live/arg_stocks")).andRespond(withServerError());

        assertThatThrownBy(() -> new Data912Cliente(builder, PROPIEDADES).cotizaciones(TipoActivo.ACCION))
                .isInstanceOf(ProveedorNoDisponibleException.class)
                .hasMessageStartingWith("data912");
    }

    @Test
    void bymaEligeLaFilaEnPesosDe24HsYUsaElCierreSiHoyNoOpero() {
        servidor.expect(requestTo("https://byma.test/free/cedears")).andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {"excludeZeroPxAndQty":true,"T0":true,"T1":true,"T2":true}"""))
                .andRespond(withSuccess("""
                        [{"symbol":"SPY","settlementType":"1","denominationCcy":"ARS","trade":44800,
                          "closingPrice":0,"previousClosingPrice":44500,"imbalance":0.0067},
                         {"symbol":"SPY","settlementType":"2","denominationCcy":"ARS","trade":45000,
                          "closingPrice":0,"previousClosingPrice":44500,"imbalance":0.0112},
                         {"symbol":"SPYD","settlementType":"2","denominationCcy":"USD","trade":28.1,
                          "closingPrice":0,"previousClosingPrice":28,"imbalance":0.0035},
                         {"symbol":"KO","settlementType":"2","denominationCcy":"ARS","trade":0,
                          "closingPrice":21300,"previousClosingPrice":21000,"imbalance":-0.0098}]
                        """, MediaType.APPLICATION_JSON));

        Map<String, Cotizacion> cotizaciones = new BymaCliente(builder, PROPIEDADES).cotizaciones(TipoActivo.CEDEAR);

        assertThat(cotizaciones).containsOnlyKeys("SPY", "KO");
        assertThat(cotizaciones.get("SPY").precio()).isEqualByComparingTo("45000");
        assertThat(cotizaciones.get("SPY").variacionDiaria()).isEqualByComparingTo("0.0112");
        assertThat(cotizaciones.get("KO").precio()).isEqualByComparingTo("21300");
        servidor.verify();
    }

    @Test
    void bymaJuntaPanelLiderYGeneralParaAcciones() {
        servidor.expect(requestTo("https://byma.test/free/leading-equity")).andRespond(withSuccess("""
                {"content":{"page_number":1,"page_count":1,"page_size":50,"total_elements_count":1},
                 "data":[{"symbol":"GGAL","settlementType":"2","denominationCcy":"ARS","trade":6100,"imbalance":0.02}]}
                """, MediaType.APPLICATION_JSON));
        servidor.expect(requestTo("https://byma.test/free/general-equity")).andRespond(withSuccess("""
                {"data":[{"symbol":"AGRO","settlementType":"2","denominationCcy":"ARS","trade":32.1,"imbalance":-0.0183}]}
                """, MediaType.APPLICATION_JSON));

        Map<String, Cotizacion> cotizaciones = new BymaCliente(builder, PROPIEDADES).cotizaciones(TipoActivo.ACCION);

        assertThat(cotizaciones).containsOnlyKeys("GGAL", "AGRO");
        servidor.verify();
    }

    @Test
    void dolarApiDevuelveCompraYVentaDelCcl() {
        servidor.expect(requestTo("https://dolar.test/v1/dolares/contadoconliqui")).andRespond(withSuccess("""
                {"moneda":"USD","casa":"contadoconliqui","nombre":"Contado con liquidación",
                 "compra":1623.6,"venta":1625.6,"fechaActualizacion":"2026-10-19T21:00:00.000Z"}
                """, MediaType.APPLICATION_JSON));

        DolarApiCliente.CotizacionDolar ccl = new DolarApiCliente(builder, PROPIEDADES).ccl();

        assertThat(ccl.compra()).isEqualByComparingTo(new BigDecimal("1623.6"));
        assertThat(ccl.venta()).isEqualByComparingTo(new BigDecimal("1625.6"));
    }
}

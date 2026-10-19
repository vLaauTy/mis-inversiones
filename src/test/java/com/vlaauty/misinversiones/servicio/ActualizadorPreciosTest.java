package com.vlaauty.misinversiones.servicio;

import com.vlaauty.misinversiones.config.PreciosProperties;
import com.vlaauty.misinversiones.dominio.Activo;
import com.vlaauty.misinversiones.dominio.FuentePrecio;
import com.vlaauty.misinversiones.dominio.NivelRiesgo;
import com.vlaauty.misinversiones.dominio.Precio;
import com.vlaauty.misinversiones.dominio.TipoActivo;
import com.vlaauty.misinversiones.integracion.Cotizacion;
import com.vlaauty.misinversiones.integracion.DolarApiCliente;
import com.vlaauty.misinversiones.integracion.ProveedorCotizaciones;
import com.vlaauty.misinversiones.integracion.ProveedorNoDisponibleException;
import com.vlaauty.misinversiones.repositorio.ActivoRepository;
import com.vlaauty.misinversiones.repositorio.DolarCclRepository;
import com.vlaauty.misinversiones.repositorio.PrecioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** Orden de fuentes, respaldo cuando una falla y que no se repita el cierre. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ActualizadorPreciosTest {

    @Autowired
    ActivoRepository activos;

    @Autowired
    PrecioRepository precios;

    @Autowired
    DolarCclRepository dolares;

    @Autowired
    PreciosProperties propiedades;

    @Autowired
    Clock reloj;

    @MockitoBean
    DolarApiCliente dolarApi;

    private final FuenteFalsa byma = new FuenteFalsa(FuentePrecio.BYMA);
    private final FuenteFalsa data912 = new FuenteFalsa(FuentePrecio.DATA912);
    private ActualizadorPrecios actualizador;

    @BeforeEach
    void preparar() {
        actualizador = new ActualizadorPrecios(List.of(data912, byma), propiedades, dolarApi, activos, precios, dolares, reloj);
        when(dolarApi.ccl()).thenReturn(new DolarApiCliente.CotizacionDolar(new BigDecimal("1620"), new BigDecimal("1625")));
    }

    private Activo activo(String ticker, TipoActivo tipo) {
        return activos.save(new Activo(ticker, ticker, tipo, NivelRiesgo.BAJO));
    }

    @Test
    void usaBymaPrimeroYData912ParaLoQueFalta() {
        Activo spy = activo("SPY", TipoActivo.CEDEAR);
        Activo ko = activo("KO", TipoActivo.CEDEAR);
        byma.precios.put("SPY", "45000");
        data912.precios.put("SPY", "44000");
        data912.precios.put("KO", "21000");

        ResultadoActualizacion r = actualizador.actualizar();

        assertThat(r.porFuente()).containsEntry(FuentePrecio.BYMA, 1).containsEntry(FuentePrecio.DATA912, 1);
        assertThat(ultimo(spy).getFuente()).isEqualTo(FuentePrecio.BYMA);
        assertThat(ultimo(spy).getPrecioArs()).isEqualByComparingTo("45000");
        assertThat(ultimo(ko).getFuente()).isEqualTo(FuentePrecio.DATA912);
        assertThat(r.dolarActualizado()).isTrue();
        assertThat(dolares.findFirstByOrderByFechaHoraDescIdDesc()).isPresent();
    }

    @Test
    void siBymaFallaTodoSaleDeData912() {
        Activo ggal = activo("GGAL", TipoActivo.ACCION);
        byma.caida = true;
        data912.precios.put("GGAL", "6100");

        ResultadoActualizacion r = actualizador.actualizar();

        assertThat(r.porFuente()).containsOnlyKeys(FuentePrecio.DATA912);
        assertThat(ultimo(ggal).getPrecioArs()).isEqualByComparingTo("6100");
    }

    @Test
    void informaLosTickersQueNingunaFuenteTiene() {
        activo("INVENTADO", TipoActivo.CEDEAR);

        ResultadoActualizacion r = actualizador.actualizar();

        assertThat(r.sinPrecio()).containsExactly("INVENTADO");
        assertThat(r.actualizados()).isZero();
    }

    @Test
    void noRepiteElMismoPrecioDelMismoDia() {
        Activo spy = activo("SPY", TipoActivo.CEDEAR);
        byma.precios.put("SPY", "45000");

        actualizador.actualizar();
        actualizador.actualizar();

        assertThat(precios.findAll()).filteredOn(p -> p.getActivo().equals(spy)).hasSize(1);
    }

    @Test
    void siElDolarFallaLosPreciosSeGuardanIgual() {
        activo("SPY", TipoActivo.CEDEAR);
        byma.precios.put("SPY", "45000");
        when(dolarApi.ccl()).thenThrow(new ProveedorNoDisponibleException("dolarapi", new RuntimeException("timeout")));

        ResultadoActualizacion r = actualizador.actualizar();

        assertThat(r.actualizados()).isEqualTo(1);
        assertThat(r.dolarActualizado()).isFalse();
    }

    private Precio ultimo(Activo activo) {
        return precios.findFirstByActivoOrderByFechaHoraDescIdDesc(activo).orElseThrow();
    }

    /** Fuente en memoria: devuelve los precios que se le cargan, o falla si está "caída". */
    static class FuenteFalsa implements ProveedorCotizaciones {

        private final FuentePrecio fuente;
        final Map<String, String> precios = new HashMap<>();
        boolean caida;

        FuenteFalsa(FuentePrecio fuente) {
            this.fuente = fuente;
        }

        @Override
        public FuentePrecio fuente() {
            return fuente;
        }

        @Override
        public Map<String, Cotizacion> cotizaciones(TipoActivo tipo) {
            if (caida) {
                throw new ProveedorNoDisponibleException(fuente.name(), new RuntimeException("sin respuesta"));
            }
            Map<String, Cotizacion> resultado = new HashMap<>();
            precios.forEach((t, p) -> resultado.put(t, new Cotizacion(t, new BigDecimal(p), new BigDecimal("0.01"))));
            return resultado;
        }
    }
}

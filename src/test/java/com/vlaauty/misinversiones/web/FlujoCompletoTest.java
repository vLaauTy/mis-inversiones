package com.vlaauty.misinversiones.web;

import com.vlaauty.misinversiones.dominio.DolarCcl;
import com.vlaauty.misinversiones.repositorio.ActivoRepository;
import com.vlaauty.misinversiones.repositorio.BolsitaRepository;
import com.vlaauty.misinversiones.repositorio.DolarCclRepository;
import com.vlaauty.misinversiones.repositorio.OperacionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FlujoCompletoTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ActivoRepository activos;

    @Autowired
    BolsitaRepository bolsitas;

    @Autowired
    OperacionRepository operaciones;

    @Autowired
    DolarCclRepository dolares;

    private Long crearActivo(String ticker) throws Exception {
        mvc.perform(post("/activos").with(csrf())
                .param("ticker", ticker).param("nombre", ticker)
                .param("tipo", "CEDEAR").param("nivelRiesgo", "BAJO"));
        return activos.findByTickerIgnoreCase(ticker).orElseThrow().getId();
    }

    private void comprar(Long activoId, String cantidad, String precio) throws Exception {
        mvc.perform(post("/operaciones/accion").with(csrf())
                        .param("fecha", "2026-10-20").param("activoId", activoId.toString()).param("tipo", "COMPRA")
                        .param("cantidad", cantidad).param("precioUnitario", precio))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void sinLoginRedirigeAlLogin() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
    }

    @Test
    void loginConUsuarioDeConfiguracion() throws Exception {
        mvc.perform(post("/login").param("username", "lauti").param("password", "cambiame").with(csrf()))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithMockUser
    void todasLasPantallasCarganVacias() throws Exception {
        for (String url : new String[]{"/", "/cartera", "/bolsitas", "/activos", "/operaciones",
                "/operaciones/nueva?tipo=ACCION", "/operaciones/nueva?tipo=PLAZO_FIJO", "/operaciones/nueva?tipo=RESERVA"}) {
            mvc.perform(get(url)).andExpect(status().isOk());
        }
    }

    @Test
    @WithMockUser
    void bolsitasInicialesAparecenEnInicio() throws Exception {
        mvc.perform(get("/"))
                .andExpect(content().string(containsString("Black Friday")))
                .andExpect(content().string(containsString("Enero en adelante")));
    }

    @Test
    @WithMockUser
    void compraActualizaCarteraYVentaDeMasFalla() throws Exception {
        mvc.perform(post("/activos").with(csrf())
                        .param("ticker", "spy").param("nombre", "ETF S&P 500")
                        .param("tipo", "CEDEAR").param("nivelRiesgo", "BAJO"))
                .andExpect(redirectedUrl("/activos"));
        Long id = activos.findByTickerIgnoreCase("SPY").orElseThrow().getId();

        mvc.perform(post("/operaciones/accion").with(csrf())
                        .param("fecha", "2026-10-20").param("activoId", id.toString()).param("tipo", "COMPRA")
                        .param("cantidad", "10").param("precioUnitario", "20000"))
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("ok", "Compra guardada."));

        // 10 × 20.000 + comisión estimada 1.089 (0,45% + IVA) = 201.089
        mvc.perform(get("/cartera"))
                .andExpect(content().string(containsString("SPY")))
                .andExpect(content().string(containsString("$201.089")));

        mvc.perform(post("/operaciones/accion").with(csrf())
                        .param("fecha", "2026-10-21").param("activoId", id.toString()).param("tipo", "VENTA")
                        .param("cantidad", "11").param("precioUnitario", "21000"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasErrors("accionForm"))
                .andExpect(content().string(containsString("pero tenés 10")));
    }

    @Test
    @WithMockUser
    void noDejaBorrarUnaCompraQueRespaldaUnaVenta() throws Exception {
        Long id = crearActivo("KO");
        comprar(id, "3", "16000");
        mvc.perform(post("/operaciones/accion").with(csrf())
                        .param("fecha", "2026-10-21").param("activoId", id.toString()).param("tipo", "VENTA")
                        .param("cantidad", "2").param("precioUnitario", "17000"))
                .andExpect(redirectedUrl("/"));
        Long compraId = operaciones.findAllCronologico().getFirst().getId();

        mvc.perform(post("/operaciones/{id}/borrar", compraId).with(csrf()))
                .andExpect(redirectedUrl("/operaciones"))
                .andExpect(flash().attribute("error", containsString("Borrá primero la venta")));
    }

    @Test
    @WithMockUser
    void depositoYPlazoFijoSumanEnLaBolsita() throws Exception {
        Long bf = bolsitas.findAll().stream().filter(b -> b.getNombre().equals("Black Friday")).findFirst().orElseThrow().getId();
        Long enero = bolsitas.findAll().stream().filter(b -> b.getNombre().equals("Enero en adelante")).findFirst().orElseThrow().getId();

        mvc.perform(post("/operaciones/reserva").with(csrf())
                        .param("bolsitaId", bf.toString()).param("tipo", "DEPOSITO")
                        .param("fecha", "2026-10-20").param("monto", "500000"))
                .andExpect(redirectedUrl("/"));

        mvc.perform(post("/operaciones/plazo-fijo").with(csrf())
                        .param("bolsitaId", enero.toString()).param("instrumento", "PLAZO_FIJO")
                        .param("entidad", "Banco Provincia").param("monto", "600000").param("tasaPct", "22")
                        .param("fechaInicio", "2026-10-20").param("plazoDias", "30"))
                .andExpect(redirectedUrl("/"));

        mvc.perform(get("/"))
                .andExpect(content().string(containsString("$1.100.000")));

        mvc.perform(get("/operaciones"))
                .andExpect(content().string(containsString("$610.849")));

        mvc.perform(post("/operaciones/reserva").with(csrf())
                        .param("bolsitaId", bf.toString()).param("tipo", "RETIRO")
                        .param("fecha", "2026-10-21").param("monto", "600000"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No podés retirar más")));
    }

    @Test
    @WithMockUser
    void siNingunaFuenteRespondeAvisaQueTickersQuedaronSinPrecio() throws Exception {
        crearActivo("SPY");

        mvc.perform(post("/cartera/precios/actualizar").with(csrf()))
                .andExpect(redirectedUrl("/cartera"))
                .andExpect(flash().attribute("error", containsString("No encontré precio para SPY")));
    }

    @Test
    @WithMockUser
    void inicioMuestraElPatrimonioEnDolaresConElCcl() throws Exception {
        Long bf = bolsitas.findAll().stream().filter(b -> b.getNombre().equals("Black Friday")).findFirst().orElseThrow().getId();
        mvc.perform(post("/operaciones/reserva").with(csrf())
                .param("bolsitaId", bf.toString()).param("tipo", "DEPOSITO")
                .param("fecha", "2026-10-20").param("monto", "500000"));
        dolares.save(new DolarCcl(LocalDateTime.of(2026, 10, 20, 17, 0), new BigDecimal("1595"), new BigDecimal("1600")));

        // 500.000 / 1.600 = 312,5 → US$ 313
        mvc.perform(get("/"))
                .andExpect(content().string(containsString("US$ 313")))
                .andExpect(content().string(containsString("CCL $1.600")));
    }
}

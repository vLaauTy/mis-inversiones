package com.vlaauty.misinversiones.web;

import com.vlaauty.misinversiones.servicio.ActivoService;
import com.vlaauty.misinversiones.servicio.ActualizadorPrecios;
import com.vlaauty.misinversiones.servicio.CarteraService;
import com.vlaauty.misinversiones.servicio.Posicion;
import com.vlaauty.misinversiones.servicio.ReglaNegocioException;
import com.vlaauty.misinversiones.servicio.ResultadoActualizacion;
import com.vlaauty.misinversiones.servicio.TotalesCartera;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** Posiciones abiertas, actualización de precios y carga manual. */
@Controller
public class CarteraController {

    private final CarteraService cartera;
    private final ActivoService activos;
    private final ActualizadorPrecios actualizador;
    private final Clock reloj;

    public CarteraController(CarteraService cartera, ActivoService activos, ActualizadorPrecios actualizador,
                             Clock reloj) {
        this.cartera = cartera;
        this.activos = activos;
        this.actualizador = actualizador;
        this.reloj = reloj;
    }

    @GetMapping("/cartera")
    public String cartera(Model model) {
        List<Posicion> posiciones = cartera.posiciones();
        model.addAttribute("seccion", "cartera");
        model.addAttribute("posiciones", posiciones);
        model.addAttribute("totales", TotalesCartera.de(posiciones));
        model.addAttribute("gananciaRealizada", cartera.gananciaRealizada());
        model.addAttribute("hoy", LocalDate.now(reloj));
        return "cartera";
    }

    /** Lo mismo que hace la tarea programada, a pedido. */
    @PostMapping("/cartera/precios/actualizar")
    public String actualizarPrecios(RedirectAttributes flash) {
        ResultadoActualizacion r = actualizador.actualizar();
        if (r.actualizados() > 0) {
            flash.addFlashAttribute("ok", "Precios al día: " + r.detalleFuentes() + ".");
        }
        if (!r.sinPrecio().isEmpty()) {
            flash.addFlashAttribute("error", "No encontré precio para " + String.join(", ", r.sinPrecio())
                    + ". Revisá que el ticker sea el de BYMA (por ejemplo SPY, GGAL).");
        } else if (r.actualizados() == 0) {
            flash.addFlashAttribute("ok", "No hay activos para actualizar.");
        }
        return "redirect:/cartera";
    }

    @PostMapping("/cartera/{activoId}/precio")
    public String actualizarPrecio(@PathVariable Long activoId, @RequestParam(required = false) BigDecimal precio,
                                   RedirectAttributes flash) {
        try {
            cartera.registrarPrecioManual(activos.buscar(activoId), precio);
            flash.addFlashAttribute("ok", "Precio actualizado.");
        } catch (ReglaNegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cartera";
    }
}

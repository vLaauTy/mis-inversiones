package com.vlaauty.misinversiones.web;

import com.vlaauty.misinversiones.servicio.ActivoService;
import com.vlaauty.misinversiones.servicio.CarteraService;
import com.vlaauty.misinversiones.servicio.Posicion;
import com.vlaauty.misinversiones.servicio.ReglaNegocioException;
import com.vlaauty.misinversiones.servicio.TotalesCartera;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/** Posiciones abiertas y carga manual de precios. */
@Controller
public class CarteraController {

    private final CarteraService cartera;
    private final ActivoService activos;

    public CarteraController(CarteraService cartera, ActivoService activos) {
        this.cartera = cartera;
        this.activos = activos;
    }

    @GetMapping("/cartera")
    public String cartera(Model model) {
        List<Posicion> posiciones = cartera.posiciones();
        model.addAttribute("seccion", "cartera");
        model.addAttribute("posiciones", posiciones);
        model.addAttribute("totales", TotalesCartera.de(posiciones));
        model.addAttribute("gananciaRealizada", cartera.gananciaRealizada());
        return "cartera";
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

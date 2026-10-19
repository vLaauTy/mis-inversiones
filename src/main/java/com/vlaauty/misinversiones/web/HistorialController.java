package com.vlaauty.misinversiones.web;

import com.vlaauty.misinversiones.servicio.BolsitaService;
import com.vlaauty.misinversiones.servicio.OperacionService;
import com.vlaauty.misinversiones.servicio.ReglaNegocioException;
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

/** Historial de operaciones, movimientos y plazos fijos; borrar errores y cobrar plazos. */
@Controller
public class HistorialController {

    private static final String REDIRECCION = "redirect:/operaciones";

    private final OperacionService operaciones;
    private final BolsitaService bolsitas;
    private final Clock reloj;

    public HistorialController(OperacionService operaciones, BolsitaService bolsitas, Clock reloj) {
        this.operaciones = operaciones;
        this.bolsitas = bolsitas;
        this.reloj = reloj;
    }

    @GetMapping("/operaciones")
    public String historial(Model model) {
        model.addAttribute("seccion", "historial");
        model.addAttribute("operaciones", operaciones.historial());
        model.addAttribute("movimientos", bolsitas.historialMovimientos());
        model.addAttribute("plazos", bolsitas.todosLosPlazos());
        model.addAttribute("hoy", LocalDate.now(reloj));
        return "historial";
    }

    @PostMapping("/operaciones/{id}/borrar")
    public String borrar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            operaciones.borrar(id);
            flash.addFlashAttribute("ok", "Operación borrada.");
        } catch (ReglaNegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return REDIRECCION;
    }

    @PostMapping("/plazos-fijos/{id}/cobrar")
    public String cobrar(@PathVariable Long id, @RequestParam(required = false) BigDecimal montoCobrado,
                         RedirectAttributes flash) {
        try {
            bolsitas.cobrarPlazoFijo(id, montoCobrado);
            flash.addFlashAttribute("ok", "Plazo fijo marcado como cobrado. La plata quedó en su bolsita.");
        } catch (ReglaNegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return REDIRECCION;
    }
}

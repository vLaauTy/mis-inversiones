package com.vlaauty.misinversiones.web;

import com.vlaauty.misinversiones.dominio.NivelRiesgo;
import com.vlaauty.misinversiones.dominio.TipoActivo;
import com.vlaauty.misinversiones.servicio.ActivoService;
import com.vlaauty.misinversiones.servicio.ReglaNegocioException;
import com.vlaauty.misinversiones.web.form.ActivoForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Catálogo de activos con su nivel de riesgo. */
@Controller
public class ActivoController {

    private final ActivoService activos;

    public ActivoController(ActivoService activos) {
        this.activos = activos;
    }

    /** @param volver "cargar" para volver a Cargar operación con el activo nuevo elegido */
    @GetMapping("/activos")
    public String lista(@RequestParam(required = false) String volver, Model model) {
        model.addAttribute("activoForm", new ActivoForm());
        model.addAttribute("volver", volver);
        return vista(model);
    }

    @PostMapping("/activos")
    public String crear(@Valid @ModelAttribute("activoForm") ActivoForm form, BindingResult errores,
                        @RequestParam(required = false) String volver, Model model, RedirectAttributes flash) {
        if (!errores.hasErrors()) {
            try {
                var activo = activos.crear(form.getTicker(), form.getNombre(), form.getTipo(), form.getNivelRiesgo());
                flash.addFlashAttribute("ok", activo.getTicker() + " agregado.");
                if ("cargar".equals(volver)) {
                    return "redirect:/operaciones/nueva?tipo=ACCION&activoId=" + activo.getId();
                }
                return "redirect:/activos";
            } catch (ReglaNegocioException e) {
                errores.reject("regla", e.getMessage());
            }
        }
        model.addAttribute("volver", volver);
        return vista(model);
    }

    @PostMapping("/activos/{id}")
    public String actualizar(@PathVariable Long id, @RequestParam String nombre, @RequestParam TipoActivo tipo,
                             @RequestParam NivelRiesgo nivelRiesgo, RedirectAttributes flash) {
        try {
            activos.actualizar(id, nombre, tipo, nivelRiesgo);
            flash.addFlashAttribute("ok", "Activo actualizado.");
        } catch (ReglaNegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/activos";
    }

    private String vista(Model model) {
        model.addAttribute("seccion", "activos");
        model.addAttribute("activos", activos.todos());
        model.addAttribute("niveles", NivelRiesgo.values());
        model.addAttribute("tiposActivo", TipoActivo.values());
        return "activos";
    }
}

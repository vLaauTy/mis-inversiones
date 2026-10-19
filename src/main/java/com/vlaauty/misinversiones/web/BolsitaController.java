package com.vlaauty.misinversiones.web;

import com.vlaauty.misinversiones.dominio.TipoBolsita;
import com.vlaauty.misinversiones.servicio.BolsitaService;
import com.vlaauty.misinversiones.servicio.ReglaNegocioException;
import com.vlaauty.misinversiones.web.form.BolsitaForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Alta y edición de bolsitas. */
@Controller
public class BolsitaController {

    private final BolsitaService bolsitas;

    public BolsitaController(BolsitaService bolsitas) {
        this.bolsitas = bolsitas;
    }

    @GetMapping("/bolsitas")
    public String lista(Model model) {
        model.addAttribute("bolsitaForm", new BolsitaForm());
        return vista(model);
    }

    @PostMapping("/bolsitas")
    public String crear(@Valid @ModelAttribute("bolsitaForm") BolsitaForm form, BindingResult errores,
                        Model model, RedirectAttributes flash) {
        if (!errores.hasErrors()) {
            try {
                bolsitas.crear(form.aDatos());
                flash.addFlashAttribute("ok", "Bolsita creada.");
                return "redirect:/bolsitas";
            } catch (ReglaNegocioException e) {
                errores.reject("regla", e.getMessage());
            }
        }
        return vista(model);
    }

    @PostMapping("/bolsitas/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("edicion") BolsitaForm form,
                             BindingResult errores, RedirectAttributes flash) {
        if (errores.hasErrors()) {
            flash.addFlashAttribute("error", errores.getAllErrors().getFirst().getDefaultMessage());
            return "redirect:/bolsitas";
        }
        try {
            bolsitas.actualizar(id, form.aDatos());
            flash.addFlashAttribute("ok", "Bolsita actualizada.");
        } catch (ReglaNegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/bolsitas";
    }

    private String vista(Model model) {
        model.addAttribute("seccion", "bolsitas");
        model.addAttribute("resumenes", bolsitas.resumenes());
        model.addAttribute("tiposBolsita", TipoBolsita.values());
        return "bolsitas";
    }
}

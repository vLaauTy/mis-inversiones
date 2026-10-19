package com.vlaauty.misinversiones.web;

import com.vlaauty.misinversiones.dominio.Instrumento;
import com.vlaauty.misinversiones.dominio.PlazoFijo;
import com.vlaauty.misinversiones.servicio.ActivoService;
import com.vlaauty.misinversiones.servicio.BolsitaService;
import com.vlaauty.misinversiones.servicio.CalculadoraComision;
import com.vlaauty.misinversiones.servicio.OperacionService;
import com.vlaauty.misinversiones.servicio.ReglaNegocioException;
import com.vlaauty.misinversiones.web.form.AccionForm;
import com.vlaauty.misinversiones.web.form.MovimientoForm;
import com.vlaauty.misinversiones.web.form.PlazoFijoForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Clock;
import java.time.LocalDate;

/** Pantalla "Cargar operación": acción o CEDEAR, plazo fijo o LECAP, y movimientos de reservas. */
@Controller
@RequestMapping("/operaciones")
public class CargaController {

    private static final String VISTA = "cargar";

    private final OperacionService operaciones;
    private final ActivoService activos;
    private final BolsitaService bolsitas;
    private final CalculadoraComision comision;
    private final Clock reloj;
    private final Formato formato;

    public CargaController(OperacionService operaciones, ActivoService activos, BolsitaService bolsitas,
                           CalculadoraComision comision, Clock reloj, Formato formato) {
        this.operaciones = operaciones;
        this.activos = activos;
        this.bolsitas = bolsitas;
        this.comision = comision;
        this.reloj = reloj;
        this.formato = formato;
    }

    @GetMapping("/nueva")
    public String nueva(@RequestParam(defaultValue = "ACCION") TipoCarga tipo,
                        @RequestParam(required = false) Long activoId,
                        @RequestParam(required = false) Long bolsitaId,
                        Model model) {
        LocalDate hoy = LocalDate.now(reloj);
        switch (tipo) {
            case ACCION -> {
                AccionForm form = new AccionForm();
                form.setFecha(hoy);
                form.setActivoId(activoId);
                model.addAttribute("accionForm", form);
            }
            case PLAZO_FIJO -> {
                PlazoFijoForm form = new PlazoFijoForm();
                form.setFechaInicio(hoy);
                form.setBolsitaId(bolsitaId);
                model.addAttribute("plazoFijoForm", form);
            }
            case RESERVA -> {
                MovimientoForm form = new MovimientoForm();
                form.setFecha(hoy);
                form.setBolsitaId(bolsitaId);
                model.addAttribute("movimientoForm", form);
            }
        }
        return vista(tipo, model);
    }

    @PostMapping("/accion")
    public String guardarAccion(@Valid @ModelAttribute("accionForm") AccionForm form, BindingResult errores,
                                Model model, RedirectAttributes flash) {
        if (!errores.hasErrors()) {
            try {
                operaciones.registrar(form.aNuevaOperacion());
                flash.addFlashAttribute("ok", form.getTipo().getEtiqueta() + " guardada.");
                return "redirect:/";
            } catch (ReglaNegocioException e) {
                errores.reject("regla", e.getMessage());
            }
        }
        return vista(TipoCarga.ACCION, model);
    }

    @PostMapping("/plazo-fijo")
    public String guardarPlazoFijo(@Valid @ModelAttribute("plazoFijoForm") PlazoFijoForm form, BindingResult errores,
                                   Model model, RedirectAttributes flash) {
        if (!errores.hasErrors()) {
            try {
                PlazoFijo plazo = bolsitas.crearPlazoFijo(form.aNuevoPlazoFijo());
                flash.addFlashAttribute("ok", plazo.getInstrumento().getEtiqueta() + " guardado. Vence el "
                        + formato.fecha(plazo.getFechaVencimiento()) + ".");
                return "redirect:/";
            } catch (ReglaNegocioException e) {
                errores.reject("regla", e.getMessage());
            }
        }
        return vista(TipoCarga.PLAZO_FIJO, model);
    }

    @PostMapping("/reserva")
    public String guardarMovimiento(@Valid @ModelAttribute("movimientoForm") MovimientoForm form, BindingResult errores,
                                    Model model, RedirectAttributes flash) {
        if (!errores.hasErrors()) {
            try {
                bolsitas.registrarMovimiento(form.aNuevoMovimiento());
                flash.addFlashAttribute("ok", form.getTipo().getEtiqueta() + " guardado.");
                return "redirect:/";
            } catch (ReglaNegocioException e) {
                errores.reject("regla", e.getMessage());
            }
        }
        return vista(TipoCarga.RESERVA, model);
    }

    private String vista(TipoCarga tipo, Model model) {
        model.addAttribute("seccion", "cargar");
        model.addAttribute("tipo", tipo);
        model.addAttribute("activos", activos.todos());
        model.addAttribute("bolsitas", bolsitas.todas());
        model.addAttribute("instrumentos", Instrumento.values());
        model.addAttribute("comisionPct", comision.getPorcentaje());
        model.addAttribute("comisionIva", comision.getIva());
        return VISTA;
    }

    /** Las tres pestañas de la pantalla. */
    public enum TipoCarga {
        ACCION, PLAZO_FIJO, RESERVA
    }
}

package com.vlaauty.misinversiones.web;

import com.vlaauty.misinversiones.servicio.InicioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Clock;
import java.time.LocalDate;

/** Pantalla de inicio: patrimonio, bolsitas, vencimientos y reparto por riesgo. */
@Controller
public class InicioController {

    private final InicioService inicio;
    private final Clock reloj;

    public InicioController(InicioService inicio, Clock reloj) {
        this.inicio = inicio;
        this.reloj = reloj;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("seccion", "inicio");
        model.addAttribute("r", inicio.resumen());
        model.addAttribute("hoy", LocalDate.now(reloj));
        return "inicio";
    }
}

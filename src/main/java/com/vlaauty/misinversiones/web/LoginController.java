package com.vlaauty.misinversiones.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Página de login propia (el POST lo resuelve Spring Security). */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}

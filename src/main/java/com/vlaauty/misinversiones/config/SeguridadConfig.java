package com.vlaauty.misinversiones.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/** Un solo usuario (vos), con login por formulario y "recordarme" para el celular. */
@Configuration
public class SeguridadConfig {

    private static final Logger log = LoggerFactory.getLogger(SeguridadConfig.class);
    private static final String PASSWORD_POR_DEFECTO = "cambiame";
    private static final int TREINTA_DIAS_EN_SEGUNDOS = 60 * 60 * 24 * 30;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService usuarios(AppProperties propiedades, PasswordEncoder encoder, Environment entorno) {
        if (PASSWORD_POR_DEFECTO.equals(propiedades.password())) {
            if (entorno.acceptsProfiles(Profiles.of("prod"))) {
                throw new IllegalStateException("Definí APP_PASSWORD antes de levantar la app en prod.");
            }
            log.warn("Estás usando la contraseña por defecto. Definí la variable de entorno APP_PASSWORD.");
        }
        return new InMemoryUserDetailsManager(User.withUsername(propiedades.usuario())
                .password(encoder.encode(propiedades.password()))
                .roles("DUENIO")
                .build());
    }

    @Bean
    public SecurityFilterChain seguridad(HttpSecurity http, AppProperties propiedades,
                                         Environment entorno) throws Exception {
        boolean consolaH2 = entorno.getProperty("spring.h2.console.enabled", Boolean.class, false);
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/login", "/manifest.webmanifest", "/icono.svg").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login?salir").permitAll())
                .rememberMe(rm -> rm.key(propiedades.claveRecordarme())
                        .tokenValiditySeconds(TREINTA_DIAS_EN_SEGUNDOS));
        if (consolaH2) {
            http.csrf(csrf -> csrf.ignoringRequestMatchers(PathRequest.toH2Console()))
                    .headers(h -> h.frameOptions(f -> f.sameOrigin()));
        }
        return http.build();
    }
}

package com.vlaauty.misinversiones.servicio;

import com.vlaauty.misinversiones.dominio.Activo;
import com.vlaauty.misinversiones.dominio.NivelRiesgo;
import com.vlaauty.misinversiones.dominio.TipoActivo;
import com.vlaauty.misinversiones.repositorio.ActivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Catálogo de acciones y CEDEARs. */
@Service
@Transactional(readOnly = true)
public class ActivoService {

    private final ActivoRepository activos;

    public ActivoService(ActivoRepository activos) {
        this.activos = activos;
    }

    public List<Activo> todos() {
        return activos.findAllByOrderByTickerAsc();
    }

    public Activo buscar(Long id) {
        return activos.findById(id).orElseThrow(() -> new ReglaNegocioException("Ese activo no existe."));
    }

    @Transactional
    public Activo crear(String ticker, String nombre, TipoActivo tipo, NivelRiesgo nivel) {
        if (activos.existsByTickerIgnoreCase(ticker.trim())) {
            throw new ReglaNegocioException("Ya tenés cargado el ticker " + ticker.trim().toUpperCase() + ".");
        }
        return activos.save(new Activo(ticker, nombre, tipo, nivel));
    }

    @Transactional
    public void actualizar(Long id, String nombre, TipoActivo tipo, NivelRiesgo nivel) {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("El nombre no puede quedar vacío.");
        }
        buscar(id).actualizar(nombre, tipo, nivel);
    }
}

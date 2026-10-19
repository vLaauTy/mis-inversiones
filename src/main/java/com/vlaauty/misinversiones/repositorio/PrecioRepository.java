package com.vlaauty.misinversiones.repositorio;

import com.vlaauty.misinversiones.dominio.Activo;
import com.vlaauty.misinversiones.dominio.Precio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrecioRepository extends JpaRepository<Precio, Long> {

    Optional<Precio> findFirstByActivoOrderByFechaHoraDescIdDesc(Activo activo);
}

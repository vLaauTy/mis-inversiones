package com.vlaauty.misinversiones.repositorio;

import com.vlaauty.misinversiones.dominio.Activo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActivoRepository extends JpaRepository<Activo, Long> {

    Optional<Activo> findByTickerIgnoreCase(String ticker);

    boolean existsByTickerIgnoreCase(String ticker);

    List<Activo> findAllByOrderByTickerAsc();
}

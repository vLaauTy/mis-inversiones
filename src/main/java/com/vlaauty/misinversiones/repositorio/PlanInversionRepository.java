package com.vlaauty.misinversiones.repositorio;

import com.vlaauty.misinversiones.dominio.PlanInversion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanInversionRepository extends JpaRepository<PlanInversion, Long> {

    Optional<PlanInversion> findFirstByOrderByIdAsc();
}

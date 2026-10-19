package com.vlaauty.misinversiones.repositorio;

import com.vlaauty.misinversiones.dominio.DolarCcl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DolarCclRepository extends JpaRepository<DolarCcl, Long> {

    Optional<DolarCcl> findFirstByOrderByFechaHoraDescIdDesc();
}

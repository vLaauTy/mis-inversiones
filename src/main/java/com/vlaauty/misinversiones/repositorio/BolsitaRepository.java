package com.vlaauty.misinversiones.repositorio;

import com.vlaauty.misinversiones.dominio.Bolsita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BolsitaRepository extends JpaRepository<Bolsita, Long> {

    List<Bolsita> findAllByOrderByIdAsc();

    boolean existsByNombreIgnoreCase(String nombre);
}

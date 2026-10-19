package com.vlaauty.misinversiones.repositorio;

import com.vlaauty.misinversiones.dominio.Bolsita;
import com.vlaauty.misinversiones.dominio.MovimientoBolsita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovimientoBolsitaRepository extends JpaRepository<MovimientoBolsita, Long> {

    List<MovimientoBolsita> findByBolsita(Bolsita bolsita);

    @Query("select m from MovimientoBolsita m join fetch m.bolsita order by m.fecha desc, m.id desc")
    List<MovimientoBolsita> findAllByOrderByFechaDescIdDesc();
}

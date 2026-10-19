package com.vlaauty.misinversiones.repositorio;

import com.vlaauty.misinversiones.dominio.Bolsita;
import com.vlaauty.misinversiones.dominio.EstadoPlazoFijo;
import com.vlaauty.misinversiones.dominio.PlazoFijo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlazoFijoRepository extends JpaRepository<PlazoFijo, Long> {

    List<PlazoFijo> findByBolsitaAndEstado(Bolsita bolsita, EstadoPlazoFijo estado);

    @Query("select p from PlazoFijo p join fetch p.bolsita where p.estado = :estado order by p.fechaVencimiento asc")
    List<PlazoFijo> findByEstadoOrderByFechaVencimientoAsc(EstadoPlazoFijo estado);

    @Query("select p from PlazoFijo p join fetch p.bolsita order by p.fechaVencimiento desc")
    List<PlazoFijo> findAllByOrderByFechaVencimientoDesc();
}

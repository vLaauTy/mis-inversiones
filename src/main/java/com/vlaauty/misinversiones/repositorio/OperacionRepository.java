package com.vlaauty.misinversiones.repositorio;

import com.vlaauty.misinversiones.dominio.Activo;
import com.vlaauty.misinversiones.dominio.Operacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OperacionRepository extends JpaRepository<Operacion, Long> {

    /** Todas las operaciones con su activo, en orden cronológico (necesario para el costo promedio). */
    @Query("select o from Operacion o join fetch o.activo order by o.fecha asc, o.id asc")
    List<Operacion> findAllCronologico();

    @Query("select o from Operacion o where o.activo = :activo order by o.fecha asc, o.id asc")
    List<Operacion> findByActivoCronologico(Activo activo);

    @Query("select o from Operacion o join fetch o.activo order by o.fecha desc, o.id desc")
    List<Operacion> findAllRecientesPrimero();
}

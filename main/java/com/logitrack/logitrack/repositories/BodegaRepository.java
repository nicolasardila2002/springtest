package com.logitrack.logitrack.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.logitrack.logitrack.entities.Bodega;

@Repository
public interface BodegaRepository extends JpaRepository<Bodega, Long>{
    
    List<Bodega> findByEncargadoId(Long encargadoId);

    List<Bodega> findByCapacidadGreaterThan(Integer capacidad);

    @Query("select b from Bodega b where b.encargado.nombre = :nombreEncargado")
    List<Bodega> findByEncargadoNombre(@Param("nombreEncargado") String nombreEncargado);

    @Modifying
    @Query("update Bodega b set b.activo = false where b.id = :id")
    void softDeleteById(@Param("id") Long id);

    @Query("select b from Bodega b where b.activo = true")
    List<Bodega> findAllActive();

    @Query("select b from Bodega b where b.id = :id and b.activo = true")
    Optional<Bodega> findByIdActive(@Param("id") Long id);
}

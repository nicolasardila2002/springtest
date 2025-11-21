package com.logitrack.logitrack.repositories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.logitrack.logitrack.entities.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long>{

    List<Producto> findByNombre(String nombre);

    // Busca ignorando mayúsculas/minúsculas
    List<Producto> findByNombreIgnoreCase(String nombre);

    // Comprueba existencia por nombre (case-insensitive)
    boolean existsByNombreIgnoreCase(String nombre);

    List<Producto> findByPrecioGreaterThan(BigDecimal precio);

    List<Producto> findByCategoria(String categoria);

    @Modifying
    @Query("update Producto p set p.activo = false where p.id = :id")
    void softDeleteById(@Param("id") Long id);

    @Query("select p from Producto p where p.activo = true")
    List<Producto> findAllActive();

    @Query("select p from Producto p where p.id = :id and p.activo = true")
    Optional<Producto> findByIdActive(@Param("id") Long id);
}

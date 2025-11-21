package com.logitrack.logitrack.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.logitrack.logitrack.entities.Inventario;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    List<Inventario> findByProductoId(Long productoId);

    List<Inventario> findByBodegaId(Long bodegaId);

    Optional<Inventario> findByProductoIdAndBodegaId(Long productoId, Long bodegaId);

    // Verifica si existe inventario para producto y bodega específicos
    boolean existsByProductoIdAndBodegaId(Long productoId, Long bodegaId);

    // Encuentra inventario por producto y bodega (util para actualizar)
    List<Inventario> findByProductoIdAndBodegaIdAndIdNot(Long productoId, Long bodegaId, Long id);
}

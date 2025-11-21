package com.logitrack.logitrack.repositories;

import com.logitrack.logitrack.entities.MovimientoInventario;
import com.logitrack.logitrack.enums.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findByTipo(TipoMovimiento tipo);
    List<MovimientoInventario> findByUsuarioId(Long usuarioId);
    List<MovimientoInventario> findByFechaBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT m FROM MovimientoInventario m WHERE m.bodegaOrigen.id = :bodegaId OR m.bodegaDestino.id = :bodegaId")
    List<MovimientoInventario> findByBodegaId(@Param("bodegaId") Long bodegaId);

    @Query("SELECT m FROM MovimientoInventario m WHERE m.usuario.nombre LIKE %:nombreUsuario%")
    List<MovimientoInventario> findByUsuarioNombre(@Param("nombreUsuario") String nombreUsuario);
}
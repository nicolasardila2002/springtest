package com.logitrack.logitrack.repositories;

import com.logitrack.logitrack.entities.MovimientoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MovimientoDetalleRepository extends JpaRepository<MovimientoDetalle, Long> {
    
    // ✅ Buscar todos los detalles de un movimiento específico
    List<MovimientoDetalle> findByMovimientoId(Long movimientoId);
    
    // ✅ Buscar todos los movimientos donde aparece un producto específico
    List<MovimientoDetalle> findByProductoId(Long productoId);
    
    // ✅ Buscar detalles por movimiento y producto
    MovimientoDetalle findByMovimientoIdAndProductoId(Long movimientoId, Long productoId);
    
    // ✅ Consulta personalizada: Productos más movidos por cantidad
    @Query("SELECT md.producto.id, md.producto.nombre, SUM(md.cantidad) as total " +
           "FROM MovimientoDetalle md " +
           "GROUP BY md.producto.id, md.producto.nombre " +
           "ORDER BY total DESC")
    List<Object[]> findProductosMasMovidos();
    
    // ✅ Consulta personalizada: Total de movimientos por producto en un rango de fechas
    @Query("SELECT md FROM MovimientoDetalle md " +
           "WHERE md.movimiento.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "AND md.producto.id = :productoId")
    List<MovimientoDetalle> findByProductoAndFechaBetween(
        @Param("productoId") Long productoId,
        @Param("fechaInicio") java.time.LocalDateTime fechaInicio,
        @Param("fechaFin") java.time.LocalDateTime fechaFin
    );
    
    // ✅ Consulta personalizada: Detalles de movimientos de entrada
    @Query("SELECT md FROM MovimientoDetalle md WHERE md.movimiento.tipo = 'ENTRADA'")
    List<MovimientoDetalle> findDetallesEntrada();
    
    // ✅ Consulta personalizada: Detalles de movimientos de salida
    @Query("SELECT md FROM MovimientoDetalle md WHERE md.movimiento.tipo = 'SALIDA'")
    List<MovimientoDetalle> findDetallesSalida();
    
    // ✅ Consulta personalizada: Detalles de movimientos de transferencia
    @Query("SELECT md FROM MovimientoDetalle md WHERE md.movimiento.tipo = 'TRANSFERENCIA'")
    List<MovimientoDetalle> findDetallesTransferencia();
    
    // ✅ Consulta personalizada: Cantidad total movida por producto
    @Query("SELECT md.producto, SUM(md.cantidad) FROM MovimientoDetalle md " +
           "WHERE md.producto.id = :productoId " +
           "GROUP BY md.producto")
    Object[] findTotalMovimientosByProducto(@Param("productoId") Long productoId);
}
package com.logitrack.logitrack.services;

import com.logitrack.logitrack.dto.InventarioDTO;
import com.logitrack.logitrack.entities.*;
import com.logitrack.logitrack.enums.TipoMovimiento;
import com.logitrack.logitrack.repositories.InventarioRepository;
import com.logitrack.logitrack.repositories.MovimientoDetalleRepository;
import com.logitrack.logitrack.repositories.MovimientoInventarioRepository;
import com.logitrack.logitrack.repositories.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovimientoService {

    private final MovimientoInventarioRepository movimientoRepository;
    private final MovimientoDetalleRepository movimientoDetalleRepository;
    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;
    private final AuthService authService;
    private final AuditoriaService auditoriaService;

    public MovimientoService(MovimientoInventarioRepository movimientoRepository,
                           MovimientoDetalleRepository movimientoDetalleRepository,
                           InventarioRepository inventarioRepository,
                           ProductoRepository productoRepository,
                           AuthService authService,
                           AuditoriaService auditoriaService) {
        this.movimientoRepository = movimientoRepository;
        this.movimientoDetalleRepository = movimientoDetalleRepository;
        this.inventarioRepository = inventarioRepository;
        this.productoRepository = productoRepository;
        this.authService = authService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public List<MovimientoInventario> findAll() {
        return movimientoRepository.findAll();
    }


    // ✅ LÓGICA DE ENTRADA DE INVENTARIO
    @Transactional
    public MovimientoInventario registrarEntrada(Long bodegaId, List<MovimientoDetalle> detalles) {
        validarDetalles(detalles);
        
        Usuario usuario = authService.getUsuarioActual();
        Bodega bodegaDestino = new Bodega();
        bodegaDestino.setId(bodegaId);

        MovimientoInventario movimiento = new MovimientoInventario(
            TipoMovimiento.ENTRADA,
            usuario,
            null, // No hay bodega origen en entrada
            bodegaDestino
        );

        // Procesar cada detalle
        for (MovimientoDetalle detalle : detalles) {
            movimiento.agregarDetalle(detalle);
            actualizarInventario(bodegaId, detalle.getProducto().getId(), detalle.getCantidad(), true);
        }

        MovimientoInventario movimientoGuardado = movimientoRepository.save(movimiento);
        
        // Auditoría
        auditoriaService.registrarCreacion(movimientoGuardado, "Entrada de inventario");
        
        return movimientoGuardado;
    }

    // ✅ LÓGICA DE SALIDA DE INVENTARIO
    @Transactional
    public MovimientoInventario registrarSalida(Long bodegaId, List<MovimientoDetalle> detalles) {
        validarDetalles(detalles);
        
        Usuario usuario = authService.getUsuarioActual();
        Bodega bodegaOrigen = new Bodega();
        bodegaOrigen.setId(bodegaId);

        MovimientoInventario movimiento = new MovimientoInventario(
            TipoMovimiento.SALIDA,
            usuario,
            bodegaOrigen,
            null // No hay bodega destino en salida
        );

        // Procesar cada detalle
        for (MovimientoDetalle detalle : detalles) {
            movimiento.agregarDetalle(detalle);
            actualizarInventario(bodegaId, detalle.getProducto().getId(), detalle.getCantidad(), false);
        }

        MovimientoInventario movimientoGuardado = movimientoRepository.save(movimiento);
        
        // Auditoría
        auditoriaService.registrarCreacion(movimientoGuardado, "Salida de inventario");
        
        return movimientoGuardado;
    }

    // ✅ LÓGICA DE TRANSFERENCIA ENTRE BODEGAS
    @Transactional
    public MovimientoInventario registrarTransferencia(Long bodegaOrigenId, Long bodegaDestinoId, List<MovimientoDetalle> detalles) {
        validarDetalles(detalles);
        
        if (bodegaOrigenId.equals(bodegaDestinoId)) {
            throw new RuntimeException("No se puede transferir a la misma bodega");
        }

        Usuario usuario = authService.getUsuarioActual();
        Bodega bodegaOrigen = new Bodega();
        bodegaOrigen.setId(bodegaOrigenId);
        
        Bodega bodegaDestino = new Bodega();
        bodegaDestino.setId(bodegaDestinoId);

        MovimientoInventario movimiento = new MovimientoInventario(
            TipoMovimiento.TRANSFERENCIA,
            usuario,
            bodegaOrigen,
            bodegaDestino
        );

        // Procesar cada detalle
        for (MovimientoDetalle detalle : detalles) {
            movimiento.agregarDetalle(detalle);
            
            // Restar de bodega origen
            actualizarInventario(bodegaOrigenId, detalle.getProducto().getId(), detalle.getCantidad(), false);
            // Sumar a bodega destino
            actualizarInventario(bodegaDestinoId, detalle.getProducto().getId(), detalle.getCantidad(), true);
        }

        MovimientoInventario movimientoGuardado = movimientoRepository.save(movimiento);
        
        // Auditoría
        auditoriaService.registrarCreacion(movimientoGuardado, 
            "Transferencia de bodega " + bodegaOrigenId + " a " + bodegaDestinoId);
        
        return movimientoGuardado;
    }

    // ✅ ACTUALIZAR INVENTARIO (Método privado)
    private void actualizarInventario(Long bodegaId, Long productoId, Integer cantidad, boolean esEntrada) {
        Inventario inventario = inventarioRepository.findByProductoIdAndBodegaId(productoId, bodegaId)
                .orElseGet(() -> {
                    // Crear nuevo registro de inventario
                    Inventario nuevoInventario = new Inventario();
                    nuevoInventario.setProductoId(productoId);
                    nuevoInventario.setBodegaId(bodegaId);
                    nuevoInventario.setStockActual(0);
                    return nuevoInventario;
                });

        int nuevoStock = esEntrada 
            ? inventario.getStockActual() + cantidad
            : inventario.getStockActual() - cantidad;

        if (nuevoStock < 0) {
            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productoId));
            throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
        }

        inventario.setStockActual(nuevoStock);
        inventarioRepository.save(inventario);
    }

    // ✅ VALIDAR DETALLES
    private void validarDetalles(List<MovimientoDetalle> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            throw new RuntimeException("El movimiento debe tener al menos un detalle");
        }

        for (MovimientoDetalle detalle : detalles) {
            if (detalle.getCantidad() <= 0) {
                throw new RuntimeException("La cantidad debe ser mayor a 0");
            }
            if (detalle.getProducto() == null || detalle.getProducto().getId() == null) {
                throw new RuntimeException("Cada detalle debe tener un producto válido");
            }
        }
    }

    // ✅ CONSULTAS Y REPORTES

    public List<MovimientoInventario> obtenerMovimientosPorBodega(Long bodegaId) {
        return movimientoRepository.findByBodegaId(bodegaId);
    }

    public List<MovimientoInventario> obtenerMovimientosPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return movimientoRepository.findByFechaBetween(fechaInicio, fechaFin);
    }

    public List<MovimientoInventario> obtenerMovimientosPorTipo(TipoMovimiento tipo) {
        return movimientoRepository.findByTipo(tipo);
    }

    public List<MovimientoInventario> obtenerMovimientosPorUsuario(Long usuarioId) {
        return movimientoRepository.findByUsuarioId(usuarioId);
    }

    public List<MovimientoDetalle> obtenerDetallesMovimiento(Long movimientoId) {
        return movimientoDetalleRepository.findByMovimientoId(movimientoId);
    }

    public List<Object[]> obtenerProductosMasMovidos() {
        return movimientoDetalleRepository.findProductosMasMovidos();
    }

    // ✅ VERIFICAR STOCK DISPONIBLE
    public boolean verificarStockDisponible(Long bodegaId, Long productoId, Integer cantidadRequerida) {
        return inventarioRepository.findByProductoIdAndBodegaId(productoId, bodegaId)
                .map(inventario -> inventario.getStockActual() >= cantidadRequerida)
                .orElse(false);
    }

    // ✅ OBTENER STOCK ACTUAL
    public Integer obtenerStockActual(Long bodegaId, Long productoId) {
        return inventarioRepository.findByProductoIdAndBodegaId(productoId, bodegaId)
                .map(Inventario::getStockActual)
                .orElse(0);
    }

    // ✅ CANCELAR MOVIMIENTO (solo para movimientos recientes)
    @Transactional
    public void cancelarMovimiento(Long movimientoId) {
        MovimientoInventario movimiento = movimientoRepository.findById(movimientoId)
                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado: " + movimientoId));

        // Revertir el movimiento
        for (MovimientoDetalle detalle : movimiento.getDetalles()) {
            revertirMovimientoEnInventario(movimiento, detalle);
        }

        movimientoRepository.delete(movimiento);
        
        // Auditoría
        auditoriaService.registrarEliminacion(movimiento, "Movimiento cancelado");
    }

    private void revertirMovimientoEnInventario(MovimientoInventario movimiento, MovimientoDetalle detalle) {
        switch (movimiento.getTipo()) {
            case ENTRADA:
                // Restar de bodega destino
                actualizarInventario(movimiento.getBodegaDestino().getId(), 
                                   detalle.getProducto().getId(), 
                                   detalle.getCantidad(), 
                                   false);
                break;
            case SALIDA:
                // Sumar a bodega origen
                actualizarInventario(movimiento.getBodegaOrigen().getId(), 
                                   detalle.getProducto().getId(), 
                                   detalle.getCantidad(), 
                                   true);
                break;
            case TRANSFERENCIA:
                // Devolver a bodega origen y quitar de destino
                actualizarInventario(movimiento.getBodegaOrigen().getId(), 
                                   detalle.getProducto().getId(), 
                                   detalle.getCantidad(), 
                                   true);
                actualizarInventario(movimiento.getBodegaDestino().getId(), 
                                   detalle.getProducto().getId(), 
                                   detalle.getCantidad(), 
                                   false);
                break;
        }
    }

    public Object listarRecientes() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listarRecientes'");
    }
}
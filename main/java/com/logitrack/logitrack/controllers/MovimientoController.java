package com.logitrack.logitrack.controllers;

import com.logitrack.logitrack.entities.MovimientoDetalle;
import com.logitrack.logitrack.entities.MovimientoInventario;
import com.logitrack.logitrack.enums.TipoMovimiento;
import com.logitrack.logitrack.services.MovimientoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/movimientos")
@CrossOrigin(origins = "*")
public class MovimientoController {

    private final MovimientoService movimientoService;

    public MovimientoController(MovimientoService movimientoService) {
        this.movimientoService = movimientoService;
    }


    // ✅ DTO para registrar movimientos
    public static class MovimientoRequest {
        private List<DetalleRequest> detalles;
        private String observaciones;

        // Getters y Setters
        public List<DetalleRequest> getDetalles() { return detalles; }
        public void setDetalles(List<DetalleRequest> detalles) { this.detalles = detalles; }
        public String getObservaciones() { return observaciones; }
        public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    }

    public static class DetalleRequest {
        private Long productoId;
        private Integer cantidad;

        // Getters y Setters
        public Long getProductoId() { return productoId; }
        public void setProductoId(Long productoId) { this.productoId = productoId; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    }

    // ✅ ENDPOINT: Registrar entrada de inventario
    @PostMapping("/entrada/{bodegaId}")
    public ResponseEntity<?> registrarEntrada(
            @PathVariable Long bodegaId,
            @Valid @RequestBody MovimientoRequest request) {
        try {
            List<MovimientoDetalle> detalles = convertirDetalles(request.getDetalles());
            MovimientoInventario movimiento = movimientoService.registrarEntrada(bodegaId, detalles);
            return ResponseEntity.ok(new MensajeResponse("Entrada registrada exitosamente. ID: " + movimiento.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al registrar entrada: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Registrar salida de inventario
    @PostMapping("/salida/{bodegaId}")
    public ResponseEntity<?> registrarSalida(
            @PathVariable Long bodegaId,
            @Valid @RequestBody MovimientoRequest request) {
        try {
            List<MovimientoDetalle> detalles = convertirDetalles(request.getDetalles());
            MovimientoInventario movimiento = movimientoService.registrarSalida(bodegaId, detalles);
            return ResponseEntity.ok(new MensajeResponse("Salida registrada exitosamente. ID: " + movimiento.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al registrar salida: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Registrar transferencia entre bodegas
    @PostMapping("/transferencia/{bodegaOrigenId}/{bodegaDestinoId}")
    public ResponseEntity<?> registrarTransferencia(
            @PathVariable Long bodegaOrigenId,
            @PathVariable Long bodegaDestinoId,
            @Valid @RequestBody MovimientoRequest request) {
        try {
            List<MovimientoDetalle> detalles = convertirDetalles(request.getDetalles());
            MovimientoInventario movimiento = movimientoService.registrarTransferencia(bodegaOrigenId, bodegaDestinoId, detalles);
            return ResponseEntity.ok(new MensajeResponse("Transferencia registrada exitosamente. ID: " + movimiento.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al registrar transferencia: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Obtener todos los movimientos
    @GetMapping
    public ResponseEntity<List<MovimientoInventario>> findAll() {
        return ResponseEntity.ok(movimientoService.findAll());
    }

    // ✅ ENDPOINT: Obtener movimientos recientes (último mes)
    @GetMapping("/recientes")
    public ResponseEntity<List<MovimientoInventario>> obtenerMovimientosRecientes() {
        try {
            List<MovimientoInventario> movimientos = movimientoService.obtenerMovimientosPorFecha(
                LocalDateTime.now().minusMonths(1), // Último mes por defecto
                LocalDateTime.now()
            );
            return ResponseEntity.ok(movimientos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ ENDPOINT: Consultar movimientos por rango de fechas
    @GetMapping("/por-fecha")
    public ResponseEntity<?> obtenerMovimientosPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            List<MovimientoInventario> movimientos = movimientoService.obtenerMovimientosPorFecha(fechaInicio, fechaFin);
            return ResponseEntity.ok(movimientos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al consultar movimientos: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Consultar movimientos por tipo
    @GetMapping("/por-tipo/{tipo}")
    public ResponseEntity<?> obtenerMovimientosPorTipo(@PathVariable TipoMovimiento tipo) {
        try {
            List<MovimientoInventario> movimientos = movimientoService.obtenerMovimientosPorTipo(tipo);
            return ResponseEntity.ok(movimientos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al consultar movimientos: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Consultar movimientos por bodega
    @GetMapping("/por-bodega/{bodegaId}")
    public ResponseEntity<?> obtenerMovimientosPorBodega(@PathVariable Long bodegaId) {
        try {
            List<MovimientoInventario> movimientos = movimientoService.obtenerMovimientosPorBodega(bodegaId);
            return ResponseEntity.ok(movimientos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al consultar movimientos: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Consultar movimientos por usuario
    @GetMapping("/por-usuario/{usuarioId}")
    public ResponseEntity<?> obtenerMovimientosPorUsuario(@PathVariable Long usuarioId) {
        try {
            List<MovimientoInventario> movimientos = movimientoService.obtenerMovimientosPorUsuario(usuarioId);
            return ResponseEntity.ok(movimientos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al consultar movimientos: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Obtener detalles de un movimiento específico
    @GetMapping("/{movimientoId}/detalles")
    public ResponseEntity<?> obtenerDetallesMovimiento(@PathVariable Long movimientoId) {
        try {
            List<MovimientoDetalle> detalles = movimientoService.obtenerDetallesMovimiento(movimientoId);
            return ResponseEntity.ok(detalles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al obtener detalles: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Productos más movidos (reporte)
    @GetMapping("/reportes/productos-mas-movidos")
    public ResponseEntity<?> obtenerProductosMasMovidos() {
        try {
            List<Object[]> productos = movimientoService.obtenerProductosMasMovidos();
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al generar reporte: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Verificar stock disponible
    @GetMapping("/verificar-stock/{bodegaId}/{productoId}/{cantidad}")
    public ResponseEntity<?> verificarStockDisponible(
            @PathVariable Long bodegaId,
            @PathVariable Long productoId,
            @PathVariable Integer cantidad) {
        try {
            boolean disponible = movimientoService.verificarStockDisponible(bodegaId, productoId, cantidad);
            return ResponseEntity.ok(new StockResponse(disponible));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al verificar stock: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Obtener stock actual
    @GetMapping("/stock-actual/{bodegaId}/{productoId}")
    public ResponseEntity<?> obtenerStockActual(
            @PathVariable Long bodegaId,
            @PathVariable Long productoId) {
        try {
            Integer stock = movimientoService.obtenerStockActual(bodegaId, productoId);
            return ResponseEntity.ok(new StockActualResponse(stock));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al obtener stock: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Cancelar movimiento (solo movimientos recientes)
    @DeleteMapping("/{movimientoId}")
    public ResponseEntity<?> cancelarMovimiento(@PathVariable Long movimientoId) {
        try {
            movimientoService.cancelarMovimiento(movimientoId);
            return ResponseEntity.ok(new MensajeResponse("Movimiento cancelado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al cancelar movimiento: " + e.getMessage()));
        }
    }

    // ✅ Método helper para convertir DTOs a entidades
    private List<MovimientoDetalle> convertirDetalles(List<DetalleRequest> detalleRequests) {
        return detalleRequests.stream()
                .map(detalleRequest -> {
                    MovimientoDetalle detalle = new MovimientoDetalle();
                    // Crear producto con solo el ID (el servicio cargará el objeto completo)
                    com.logitrack.logitrack.entities.Producto producto = new com.logitrack.logitrack.entities.Producto();
                    producto.setId(detalleRequest.getProductoId());
                    detalle.setProducto(producto);
                    detalle.setCantidad(detalleRequest.getCantidad());
                    return detalle;
                })
                .toList();
    }

    // ✅ Clases de respuesta
    public static class MensajeResponse {
        private String mensaje;
        public MensajeResponse(String mensaje) { this.mensaje = mensaje; }
        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    }

    public static class ErrorResponse {
        private String error;
        public ErrorResponse(String error) { this.error = error; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    public static class StockResponse {
        private boolean disponible;
        public StockResponse(boolean disponible) { this.disponible = disponible; }
        public boolean isDisponible() { return disponible; }
        public void setDisponible(boolean disponible) { this.disponible = disponible; }
    }

    public static class StockActualResponse {
        private Integer stock;
        public StockActualResponse(Integer stock) { this.stock = stock; }
        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
    }

    
    @GetMapping("/recientes")
    public ResponseEntity<List<MovimientoInventario>> obtenerMovimientosRecientes10() {
        try {
            List<MovimientoInventario> movimientos = movimientoService.obtenerMovimientosPorFecha(
                LocalDateTime.now().minusNanos(10), // Último mes por defecto
                LocalDateTime.now()
            );
            return ResponseEntity.ok(movimientos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}

//   @GetMapping
//public ResponseEntity<List<MovimientoInventario>> findAll() {
//    return ResponseEntity.ok(movimientoService.findAll());
//
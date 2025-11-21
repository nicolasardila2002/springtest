package com.logitrack.logitrack.test;

import com.logitrack.logitrack.entities.MovimientoInventario;
import com.logitrack.logitrack.services.MovimientoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.logitrack.logitrack.enums.TipoMovimiento;
import com.logitrack.logitrack.controllers.AuthController.ErrorResponse;


@RestController
@RequestMapping("/api/movimientos")
@CrossOrigin(origins = "*")
public class ReporteController {

    private final MovimientoService movimientoService;

    public ReporteController(MovimientoService movimientoService) {
        this.movimientoService = movimientoService;
    }

    
    @GetMapping("/por-tipo/{tipo}")
    public ResponseEntity<?> obtenerMovimientosPorTipo(@PathVariable TipoMovimiento tipo) {
        try {
            List<MovimientoInventario> movimientos = movimientoService.obtenerMovimientosPorTipo(tipo);
            return ResponseEntity.ok(movimientos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al consultar movimientos: " + e.getMessage()));
        }
    }
}    

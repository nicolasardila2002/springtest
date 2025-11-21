package com.logitrack.logitrack.test;

import com.logitrack.logitrack.entities.MovimientoInventario;
import com.logitrack.logitrack.services.MovimientoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;




@RestController
@RequestMapping("/api/movimientos")
@CrossOrigin(origins = "*")
public class MovimientoControllerTEST {

    private final MovimientoService movimientoService;

    public MovimientoControllerTEST(MovimientoService movimientoService) {
        this.movimientoService = movimientoService;
    }

    
    @GetMapping("/recientes")
    public ResponseEntity<List<MovimientoInventario>> obtenerMovimientosRecientes10() {
        try {
            List<MovimientoInventario> movimientos = movimientoService.obtenerMovimientosPorFecha(
                LocalDateTime.now().minusNanos(10), 
                LocalDateTime.now()
            );
            return ResponseEntity.ok(movimientos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}


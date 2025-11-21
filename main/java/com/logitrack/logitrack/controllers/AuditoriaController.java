package com.logitrack.logitrack.controllers;

import com.logitrack.logitrack.entities.AuditoriaCambios;
import com.logitrack.logitrack.enums.TipoOperacion;
import com.logitrack.logitrack.services.AuditoriaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
@CrossOrigin(origins = "*")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    // ✅ ENDPOINT: Obtener todas las auditorías
    @GetMapping
    public ResponseEntity<List<AuditoriaCambios>> obtenerTodasAuditorias() {
        try {
            List<AuditoriaCambios> auditorias = auditoriaService.obtenerTodasAuditorias();
            return ResponseEntity.ok(auditorias);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ ENDPOINT: Consultar auditorías por usuario
    @GetMapping("/por-usuario/{usuarioId}")
    public ResponseEntity<?> obtenerAuditoriasPorUsuario(@PathVariable Long usuarioId) {
        try {
            List<AuditoriaCambios> auditorias = auditoriaService.obtenerAuditoriasPorUsuario(usuarioId);
            return ResponseEntity.ok(auditorias);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al consultar auditorías: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Consultar auditorías por tipo de operación
    @GetMapping("/por-operacion/{tipoOperacion}")
    public ResponseEntity<?> obtenerAuditoriasPorOperacion(@PathVariable TipoOperacion tipoOperacion) {
        try {
            List<AuditoriaCambios> auditorias = auditoriaService.obtenerAuditoriasPorOperacion(tipoOperacion);
            return ResponseEntity.ok(auditorias);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al consultar auditorías: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Consultar auditorías por entidad afectada
    @GetMapping("/por-entidad/{entidad}")
    public ResponseEntity<?> obtenerAuditoriasPorEntidad(@PathVariable String entidad) {
        try {
            List<AuditoriaCambios> auditorias = auditoriaService.obtenerAuditoriasPorEntidad(entidad);
            return ResponseEntity.ok(auditorias);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al consultar auditorías: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Consultar auditorías por rango de fechas
    @GetMapping("/por-fecha")
    public ResponseEntity<?> obtenerAuditoriasPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            List<AuditoriaCambios> auditorias = auditoriaService.obtenerAuditoriasPorFecha(fechaInicio, fechaFin);
            return ResponseEntity.ok(auditorias);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al consultar auditorías: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Consultar auditorías por usuario y rango de fechas
    @GetMapping("/por-usuario-fecha/{usuarioId}")
    public ResponseEntity<?> obtenerAuditoriasPorUsuarioYFecha(
            @PathVariable Long usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            List<AuditoriaCambios> auditorias = auditoriaService.obtenerAuditoriasPorUsuarioYFecha(usuarioId, fechaInicio, fechaFin);
            return ResponseEntity.ok(auditorias);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al consultar auditorías: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Consultar auditorías de una entidad específica
    @GetMapping("/por-entidad-id")
    public ResponseEntity<?> obtenerAuditoriasPorEntidadYId(
            @RequestParam String entidad,
            @RequestParam Long idEntidad) {
        try {
            List<AuditoriaCambios> auditorias = auditoriaService.obtenerAuditoriasPorEntidadYId(entidad, idEntidad);
            return ResponseEntity.ok(auditorias);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al consultar auditorías: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Últimas auditorías (más recientes primero)
    @GetMapping("/ultimas")
    public ResponseEntity<?> obtenerUltimasAuditorias() {
        try {
            List<AuditoriaCambios> auditorias = auditoriaService.obtenerUltimasAuditorias();
            return ResponseEntity.ok(auditorias);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al consultar auditorías: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Auditorías de hoy
    @GetMapping("/hoy")
    public ResponseEntity<?> obtenerAuditoriasDeHoy() {
        try {
            List<AuditoriaCambios> auditorias = auditoriaService.obtenerAuditoriasDeHoy();
            return ResponseEntity.ok(auditorias);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al consultar auditorías: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Resumen de operaciones por usuario
    @GetMapping("/reportes/resumen-operaciones")
    public ResponseEntity<?> obtenerResumenOperacionesPorUsuario() {
        try {
            List<Object[]> resumen = auditoriaService.obtenerResumenOperacionesPorUsuario();
            return ResponseEntity.ok(resumen);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al generar reporte: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Entidades más auditadas
    @GetMapping("/reportes/entidades-mas-auditadas")
    public ResponseEntity<?> obtenerEntidadesMasAuditadas() {
        try {
            List<Object[]> entidades = auditoriaService.obtenerEntidadesMasAuditadas();
            return ResponseEntity.ok(entidades);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al generar reporte: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Buscar auditorías por descripción
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarAuditoriasPorDescripcion(@RequestParam String texto) {
        try {
            List<AuditoriaCambios> auditorias = auditoriaService.buscarAuditoriasPorDescripcion(texto);
            return ResponseEntity.ok(auditorias);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al buscar auditorías: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Obtener auditoría por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerAuditoriaPorId(@PathVariable Long id) {
        try {
            AuditoriaCambios auditoria = auditoriaService.obtenerAuditoriaPorId(id);
            return ResponseEntity.ok(auditoria);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al obtener auditoría: " + e.getMessage()));
        }
    }

    // ✅ Clases de respuesta
    public static class ErrorResponse {
        private String error;
        public ErrorResponse(String error) { this.error = error; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
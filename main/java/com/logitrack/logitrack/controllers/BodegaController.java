package com.logitrack.logitrack.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logitrack.logitrack.dto.BodegaDTO;
import com.logitrack.logitrack.services.BodegaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;




@RestController
@RequestMapping("/api/bodegas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BodegaController {

    private final BodegaService bodegaService;

    @GetMapping
    public ResponseEntity<List<BodegaDTO>> findAll() {
        return ResponseEntity.ok(bodegaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BodegaDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(bodegaService.findById(id));
    }

    @GetMapping("/encargado/id/{encargadoId}")
    public ResponseEntity<List<BodegaDTO>> findByEncargadoId(@PathVariable Long encargadoId) {
        return ResponseEntity.ok(bodegaService.findByEncargadoId(encargadoId));
    }

    @GetMapping("/encargado/nombre/{encargadoNombre}")
    public ResponseEntity<List<BodegaDTO>> findByEncargadoNombre(@PathVariable String encargadoNombre) {
        return ResponseEntity.ok(bodegaService.findByEncargadoNombre(encargadoNombre));
    }

    @GetMapping("/capacidad/{cantidad}")
    public ResponseEntity<List<BodegaDTO>> findByCapacidadGreaterThan(@PathVariable Integer cantidad) {
        return ResponseEntity.ok(bodegaService.findByCapacidadGreaterThan(cantidad));
    }

    @PostMapping
    public ResponseEntity<BodegaDTO> create(@Valid @RequestBody BodegaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bodegaService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BodegaDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody BodegaDTO dto) {
        return ResponseEntity.ok(bodegaService.update(id, dto));
    }
        
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bodegaService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
}

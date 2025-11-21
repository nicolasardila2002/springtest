package com.logitrack.logitrack.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logitrack.logitrack.dto.InventarioDTO;
import com.logitrack.logitrack.services.InventarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InventarioController {

    private final InventarioService inventarioService;

    @GetMapping
    public ResponseEntity<List<InventarioDTO>> findAll() {
        return ResponseEntity.ok(inventarioService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventarioDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(inventarioService.findById(id));
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<InventarioDTO>> findByProductoId(@PathVariable Long productoId) {
        return ResponseEntity.ok(inventarioService.findByProductoId(productoId));
    }

    @GetMapping("/bodega/{bodegaId}")
    public ResponseEntity<List<InventarioDTO>> findByBodegaId(@PathVariable Long bodegaId) {
        return ResponseEntity.ok(inventarioService.findByBodegaId(bodegaId));
    }

    @GetMapping("/producto/{productoId}/bodega/{bodegaId}")
    public ResponseEntity<InventarioDTO> findByProductoAndBodega(@PathVariable Long productoId, @PathVariable Long bodegaId) {
        return ResponseEntity.ok(inventarioService.findByProductoAndBodega(productoId, bodegaId));
    }

    @PostMapping
    public ResponseEntity<InventarioDTO> create(@Valid @RequestBody InventarioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventarioDTO> update(@PathVariable Long id, @Valid @RequestBody InventarioDTO dto) {
        return ResponseEntity.ok(inventarioService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        inventarioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

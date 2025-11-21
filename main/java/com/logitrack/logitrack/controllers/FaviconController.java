package com.logitrack.logitrack.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FaviconController {

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        // Responder con 204 No Content para evitar errores 500 cuando no hay favicon
        return ResponseEntity.noContent().build();
    }
}

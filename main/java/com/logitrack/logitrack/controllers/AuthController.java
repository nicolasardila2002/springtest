package com.logitrack.logitrack.controllers;

import com.logitrack.logitrack.dto.LoginRequestDTO;
import com.logitrack.logitrack.dto.JwtResponseDTO;
import com.logitrack.logitrack.dto.RegistroUsuarioDTO;
import com.logitrack.logitrack.entities.Usuario;
import com.logitrack.logitrack.services.AuthService;
import com.logitrack.logitrack.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Para desarrollo - en producción especificar origins
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    public AuthController(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    // ✅ ENDPOINT: /auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            JwtResponseDTO jwtResponse = authService.login(loginRequest);
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Error en login: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: /auth/register
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistroUsuarioDTO registroDTO) {
        try {
            // Verificar si el usuario actual tiene permisos para crear usuarios
            // (opcional: puedes agregar validación de roles aquí)
            
            Usuario nuevoUsuario = usuarioService.crearUsuario(registroDTO);
            
            return ResponseEntity.ok(new MensajeResponse(
                "Usuario registrado exitosamente: " + nuevoUsuario.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Error en registro: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Verificar token (opcional)
    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken() {
        try {
            Usuario usuarioActual = authService.getUsuarioActual();
            return ResponseEntity.ok(new MensajeResponse(
                "Token válido para usuario: " + usuarioActual.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(new ErrorResponse("Token inválido: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Obtener usuario actual
    @GetMapping("/me")
    public ResponseEntity<?> getUsuarioActual() {
        try {
            Usuario usuario = authService.getUsuarioActual();
            // No retornar el password hash por seguridad
            usuario.setPasswordHash(null);
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(new ErrorResponse("Usuario no autenticado: " + e.getMessage()));
        }
    }

    // ✅ ENDPOINT: Logout (simbólico - el cliente debe eliminar el token)
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(new MensajeResponse("Logout exitoso"));
    }

    // Clases internas para respuestas
    public static class MensajeResponse {
        private String mensaje;
        
        public MensajeResponse(String mensaje) {
            this.mensaje = mensaje;
        }
        
        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    }

    public static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
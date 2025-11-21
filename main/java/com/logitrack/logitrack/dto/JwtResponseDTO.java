package com.logitrack.logitrack.dto;

import com.logitrack.logitrack.entities.Usuario;
import com.logitrack.logitrack.enums.Rol;

public class JwtResponseDTO {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String nombre;
    private String email;
    private Rol rol;
    
    // Constructores
    public JwtResponseDTO() {}
    
    public JwtResponseDTO(String token, Usuario usuario) {
        this.token = token;
        this.id = usuario.getId();
        this.nombre = usuario.getNombre();
        this.email = usuario.getEmail();
        this.rol = usuario.getRol();
    }
    
    // Getters y Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
}
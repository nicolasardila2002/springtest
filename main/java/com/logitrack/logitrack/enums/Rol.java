package com.logitrack.logitrack.enums;

public enum Rol {
    ADMIN("ADMIN"),
    EMPLEADO("EMPLEADO");
    
    private final String valor;
    
    Rol(String valor) {
        this.valor = valor;
    }
    
    public String getValor() {
        return valor;
    }
    
    public static Rol fromString(String text) {
        for (Rol rol : Rol.values()) {
            if (rol.valor.equalsIgnoreCase(text)) {
                return rol;
            }
        }
        throw new IllegalArgumentException("Rol no válido: " + text);
    }
}
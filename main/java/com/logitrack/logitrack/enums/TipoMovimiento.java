package com.logitrack.logitrack.enums;

public enum TipoMovimiento {
    ENTRADA("ENTRADA"),
    SALIDA("SALIDA"), 
    TRANSFERENCIA("TRANSFERENCIA");
    
    private final String valor;
    
    TipoMovimiento(String valor) {
        this.valor = valor;
    }
    
    public String getValor() {
        return valor;
    }
    
    public static TipoMovimiento fromString(String text) {
        for (TipoMovimiento tipo : TipoMovimiento.values()) {
            if (tipo.valor.equalsIgnoreCase(text)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de movimiento no válido: " + text);
    }
}
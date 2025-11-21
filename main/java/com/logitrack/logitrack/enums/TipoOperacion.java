package com.logitrack.logitrack.enums;

public enum TipoOperacion {
    INSERT("INSERT"),
    UPDATE("UPDATE"),
    DELETE("UPDATE");
    
    private final String valor;
    
    TipoOperacion(String valor) {
        this.valor = valor;
    }
    
    public String getValor() {
        return valor;
    }
    
    public static TipoOperacion fromString(String text) {
        for (TipoOperacion operacion : TipoOperacion.values()) {
            if (operacion.valor.equalsIgnoreCase(text)) {
                return operacion;
            }
        }
        throw new IllegalArgumentException("Tipo de operación no válido: " + text);
    }
}
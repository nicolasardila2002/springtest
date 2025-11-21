package com.logitrack.logitrack.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede tener mas de 100 caracteres")
    private String nombre;

    @NotBlank(message = "La categoria es obligatoria")
    @Size(max = 50, message = "La categoria no puede tener mas de 50 caracteres")
    private String categoria;

    @NotNull(message = "El precio es obligatorio")
    @Digits(integer = 8, fraction = 2, message = "El precio debe tener como máximo 8 dígitos enteros y 2 decimales")
    @DecimalMin(value = "0.00", inclusive = true, message = "El precio no puede ser negativo")
    private BigDecimal precio;

    private boolean activo;

    private LocalDateTime createdAt;

    private LocalDateTime updateAt;
}

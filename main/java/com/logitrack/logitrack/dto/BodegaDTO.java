package com.logitrack.logitrack.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BodegaDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede tener mas de 100 caracteres")
    private String nombre;

    private String ubicacion;

    @Positive(message = "La capacidad debe de ser mayor que 0")
    private Integer capacidad;

    @NotNull(message = "El encargado es obligatorio")
    private Long encargadoId;

    private boolean activo;

    private LocalDateTime createdAt;

    private LocalDateTime updaateAt;
}

package com.logitrack.logitrack.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioDTO {

    private Long id;

    @NotNull(message = "El id del producto es obligatorio")
    private Long productoId;

    @NotNull(message = "El id de la bodega es obligatorio")
    private Long bodegaId;

    @NotNull(message = "El stock actual es obligatorio")
    @Min(value = 0, message = "El stock actual no puede ser negativo")
    private Integer stockActual;
}

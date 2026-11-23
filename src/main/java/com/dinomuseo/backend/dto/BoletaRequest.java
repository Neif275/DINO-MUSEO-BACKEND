package com.dinomuseo.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record BoletaRequest(
        @NotNull(message = "La fecha de visita es obligatoria")
        @FutureOrPresent(message = "La fecha de visita no puede ser en el pasado")
        LocalDate fechaVisita,

        @NotEmpty(message = "Debe elegir al menos una entrada")
        List<@Valid Item> items
) {

    public record Item(
            @NotNull(message = "El producto es obligatorio")
            Long productoId,

            @NotNull(message = "La cantidad es obligatoria")
            @Min(value = 1, message = "La cantidad minima es 1")
            Integer cantidad
    ){

    }
}

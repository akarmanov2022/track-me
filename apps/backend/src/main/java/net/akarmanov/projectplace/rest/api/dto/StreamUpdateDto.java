package net.akarmanov.projectplace.rest.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record StreamUpdateDto(
        @NotBlank
        String name,
        @NotNull
        @Future
        LocalDate endDate
) {
}

package com.kanban.models.card.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CardUpdateRequest(
        @NotBlank(message = "title is required")
        @Size(max = 150, message = "title must be at most {max} characters")
        String title,

        String text,

        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "colorCode must be a hex color")
        String colorCode
) {
}

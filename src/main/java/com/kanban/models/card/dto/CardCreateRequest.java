package com.kanban.models.card.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CardCreateRequest(
        @NotNull(message = "taskListId is required")
        Long taskListId,

        @NotBlank(message = "title is required")
        @Size(max = 150, message = "title must be at most {max} characters")
        String title,

        String text
) {
}

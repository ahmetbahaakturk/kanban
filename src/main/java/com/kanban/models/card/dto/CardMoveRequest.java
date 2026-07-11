package com.kanban.models.card.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CardMoveRequest(
        @NotNull(message = "targetTaskListId is required")
        Long targetTaskListId,

        @NotNull(message = "targetPosition is required")
        @Min(value = 1, message = "targetPosition must be at least {value}")
        Integer targetPosition
) {
}

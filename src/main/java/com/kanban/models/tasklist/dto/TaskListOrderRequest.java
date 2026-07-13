package com.kanban.models.tasklist.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TaskListOrderRequest(
        @NotNull(message = "taskListId is required")
        Long taskListId,

        @NotNull(message = "cardIds is required")
        List<@NotNull(message = "card id is required") Long> cardIds
) {
}

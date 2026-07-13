package com.kanban.models.tasklist.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TaskListsOrderRequest(
        @NotEmpty(message = "taskLists must contain at least one task list")
        List<@NotNull(message = "task list is required") @Valid TaskListOrderRequest> taskLists
) {
}

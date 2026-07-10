package com.kanban.models.tasklist.dto;

import com.kanban.models.card.dto.CardResponse;
import com.kanban.models.tasklist.TaskListType;

import java.util.List;

public record TaskListResponse(
        Long id,
        TaskListType type,
        List<CardResponse> cards
) {
}

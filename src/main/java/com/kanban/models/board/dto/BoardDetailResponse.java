package com.kanban.models.board.dto;

import com.kanban.models.tasklist.dto.TaskListResponse;

import java.time.Instant;
import java.util.List;

public record BoardDetailResponse(
        String publicId,
        Instant createdAt,
        List<TaskListResponse> taskLists
) {
}

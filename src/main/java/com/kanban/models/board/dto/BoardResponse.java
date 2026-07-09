package com.kanban.models.board.dto;

import java.time.Instant;

public record BoardResponse(
        String publicId,
        Instant createdAt
) {
}

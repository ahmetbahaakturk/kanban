package com.kanban.models.board.dto;

import java.time.Instant;


//alt dto
public record BoardResponse(
        String publicId,
        Instant createdAt
) {
}

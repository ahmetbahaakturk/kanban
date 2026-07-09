package com.kanban.models.board.dto;

import com.kanban.models.board.BoardUtils;
import jakarta.validation.constraints.Size;

public record BoardCreateRequest(
        @Size(
                min = BoardUtils.MIN_PUBLIC_ID_LENGTH,
                max = BoardUtils.MAX_PUBLIC_ID_LENGTH
        )
        String publicId
) {
}

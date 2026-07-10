package com.kanban.models.board.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record BoardCreateRequest(
        @Valid
        @NotNull(message = "publicId is required")
        PublicId publicId
) {
    public BoardCreateRequest(String publicId) {
        this(new PublicId(publicId));
    }

    public String publicIdValue() {
        return publicId.value();
    }
}

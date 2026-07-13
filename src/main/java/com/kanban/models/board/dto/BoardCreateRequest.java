package com.kanban.models.board.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record BoardCreateRequest(
        @Valid
        @NotNull(message = "publicId is required")
        PublicId publicId
) {
    public BoardCreateRequest(String publicId) {
        //Buradaki String publicId değerini Wrapper PublicId'ye dönüştürüp öyle dto oluşturulur
        this(new PublicId(publicId));
    }

    public String publicIdValue() {
        return publicId.value();
    }
}

package com.kanban.models.card.dto;

public record CardResponse(
        Long id,
        String title,
        String text,
        String colorCode,
        Integer position
) {
}

package com.kanban.models.board;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BoardUtilsTests {

    @Test
    void generatePublicIdReturnsUrlSafeValueWithinLengthBounds() {
        String publicId = BoardUtils.generatePublicId();

        assertThat(publicId)
            .hasSizeBetween(BoardUtils.MIN_GENERATED_PUBLIC_ID_LENGTH, BoardUtils.MAX_GENERATED_PUBLIC_ID_LENGTH)
            .matches("^[A-Za-z0-9_-]+$");
    }
}

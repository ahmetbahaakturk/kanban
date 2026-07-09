package com.kanban.models.board;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BoardUtilsTests {

    @Test
    void generatePublicIdReturnsUrlSafeValueWithinLengthBounds() {
        String publicId = BoardUtils.generatePublicId();

        assertThat(publicId)
            .hasSizeBetween(BoardUtils.MIN_LENGTH, BoardUtils.MAX_LENGTH)
            .matches("^[A-Za-z0-9_-]+$");
    }
}

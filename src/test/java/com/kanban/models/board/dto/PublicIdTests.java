package com.kanban.models.board.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class PublicIdTests {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsUrlSafePublicId() {
        PublicId publicId = new PublicId("board-1._~A9");

        assertThat(validator.validate(publicId)).isEmpty();
    }

    @Test
    void rejectsNonUrlSafePublicId() {
        PublicId publicId = new PublicId("board 1");

        assertThat(validator.validate(publicId))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("value");
    }

    @Test
    void rejectsPublicIdShorterThanMinimumLength() {
        PublicId publicId = new PublicId("abcd");

        assertThat(validator.validate(publicId)).isNotEmpty();
    }
}

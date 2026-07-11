package com.kanban.models.card.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class CardMoveRequestTests {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsValidMoveRequest() {
        CardMoveRequest request = new CardMoveRequest(1L, 1);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsMissingTargetTaskListId() {
        CardMoveRequest request = new CardMoveRequest(null, 1);

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("targetTaskListId");
    }

    @Test
    void rejectsMissingTargetPosition() {
        CardMoveRequest request = new CardMoveRequest(1L, null);

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("targetPosition");
    }

    @Test
    void rejectsTargetPositionBelowOne() {
        CardMoveRequest request = new CardMoveRequest(1L, 0);

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("targetPosition");
    }
}

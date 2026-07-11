package com.kanban.models.card.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class CardCreateRequestTests {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsValidCreateRequest() {
        CardCreateRequest request = new CardCreateRequest(1L, "Card title", "Card text");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsMissingTaskListId() {
        CardCreateRequest request = new CardCreateRequest(null, "Card title", "Card text");

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("taskListId");
    }

    @Test
    void rejectsBlankTitle() {
        CardCreateRequest request = new CardCreateRequest(1L, " ", "Card text");

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("title");
    }
}

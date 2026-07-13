package com.kanban.models.card.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class CardUpdateRequestTests {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsValidUpdateRequest() {
        CardUpdateRequest request = new CardUpdateRequest("Card title", "Card text", "#327edc");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void acceptsMissingColorCode() {
        CardUpdateRequest request = new CardUpdateRequest("Card title", "Card text", null);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsBlankTitle() {
        CardUpdateRequest request = new CardUpdateRequest(" ", "Card text", "#327edc");

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("title");
    }

    @Test
    void rejectsInvalidColorCode() {
        CardUpdateRequest request = new CardUpdateRequest("Card title", "Card text", "blue");

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("colorCode");
    }
}

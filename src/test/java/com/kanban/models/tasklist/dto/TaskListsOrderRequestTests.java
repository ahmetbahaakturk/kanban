package com.kanban.models.tasklist.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TaskListsOrderRequestTests {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsCompleteTaskListOrder() {
        TaskListsOrderRequest request = new TaskListsOrderRequest(List.of(
                new TaskListOrderRequest(1L, List.of(10L, 11L))
        ));

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void acceptsTaskListWithoutCards() {
        TaskListsOrderRequest request = new TaskListsOrderRequest(List.of(
                new TaskListOrderRequest(1L, List.of())
        ));

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsEmptyTaskLists() {
        TaskListsOrderRequest request = new TaskListsOrderRequest(List.of());

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("taskLists");
    }

    @Test
    void rejectsMissingTaskListId() {
        TaskListsOrderRequest request = new TaskListsOrderRequest(List.of(
                new TaskListOrderRequest(null, List.of())
        ));

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("taskLists[0].taskListId");
    }

    @Test
    void rejectsMissingTaskList() {
        TaskListsOrderRequest request = new TaskListsOrderRequest(Collections.singletonList(null));

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("taskLists[0].<list element>");
    }

    @Test
    void rejectsMissingCardIds() {
        TaskListsOrderRequest request = new TaskListsOrderRequest(List.of(
                new TaskListOrderRequest(1L, null)
        ));

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("taskLists[0].cardIds");
    }
}

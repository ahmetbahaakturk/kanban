package com.kanban.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTests {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleAlreadyExistsReturnsConflictResponse() {
        ResponseEntity<ErrorResponse> response = handler.handleAlreadyExists(
                new AlreadyExistsException("Board already exists")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(response.getBody().error()).isEqualTo(HttpStatus.CONFLICT.getReasonPhrase());
        assertThat(response.getBody().message()).isEqualTo("Board already exists");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void handleNotFoundReturnsNotFoundResponse() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(
                new NotFoundException("Board not found")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getBody().error()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
        assertThat(response.getBody().message()).isEqualTo("Board not found");
        assertThat(response.getBody().timestamp()).isNotNull();
    }
}

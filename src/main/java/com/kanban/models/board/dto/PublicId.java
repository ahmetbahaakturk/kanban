package com.kanban.models.board.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.kanban.models.board.BoardUtils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Centralizes publicId validation for requests that carry a publicId.
public record PublicId(
        @NotBlank(message = "publicId is required")
        @Size(
                min = BoardUtils.MIN_PUBLIC_ID_LENGTH,
                max = BoardUtils.MAX_PUBLIC_ID_LENGTH,
                message = "publicId must be between {min} and {max} characters"
        )
        @Pattern(
                regexp = BoardUtils.URL_SAFE_PUBLIC_ID_REGEX,
                message = "publicId must contain only URL-safe characters"
        )
        String value
) {


    //Eğer bir publicId değeri başka bir dtonun içine gömülü ise buraya çevrilmesi için giriliyor.
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public PublicId {
    }
}

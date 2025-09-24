package se.lexicon.flightbooking_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QueryDto(
        @NotNull(message = "query can not be null")
        @NotBlank(message = "query can not be empty")
        @Size(max = 1000, message = "query must be less than 1000 characters")
        String query,
        @NotNull(message = "conversationId can not be null")
        @NotBlank(message = "conversationId can not be empty")
        String conversationId
) {
}

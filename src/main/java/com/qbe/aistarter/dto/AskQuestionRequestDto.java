package com.qbe.aistarter.dto;

import com.qbe.aistarter.enums.ResponseLevelDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AskQuestionRequestDto(
        UUID conversationId,
        @NotBlank(message = "Question must not be blank") String question,
        @NotNull(message = "Response level must not be blank") ResponseLevelDto responseLevel) {}

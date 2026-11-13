package com.qbe.aistarter.dto;

import com.qbe.aistarter.enums.TravelProfile;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record TravelRequestDto(
        @NotBlank String conversationId,
        @NotBlank String destination,
        @Min(1) int durationInDays,
        @Min(0) double budgetInEuros,
        @NotEmpty List<String> interests,
        @NotNull TravelProfile profile) {}

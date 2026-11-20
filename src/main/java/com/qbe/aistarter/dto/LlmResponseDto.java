package com.qbe.aistarter.dto;

import java.time.Instant;
import java.util.UUID;

public record LlmResponseDto(UUID conversationId, String answer, Instant generatedAt) {}

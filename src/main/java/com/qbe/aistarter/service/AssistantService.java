package com.qbe.aistarter.service;

import com.qbe.aistarter.dto.LlmResponseDto;
import com.qbe.aistarter.enums.ResponseLevelDto;
import java.util.UUID;

public interface AssistantService {

    LlmResponseDto askWithPromptTemplate(UUID conversationId, String question, ResponseLevelDto responseLevel);

    LlmResponseDto askWithoutPromptTemplate(UUID conversationId, String question);
}

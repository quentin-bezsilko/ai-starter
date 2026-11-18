package com.qbe.aistarter.service;

import com.qbe.aistarter.dto.ExpertAnswerResponseDto;
import com.qbe.aistarter.enums.ResponseLevelDto;
import java.util.UUID;

public interface AssistantService {

    ExpertAnswerResponseDto askWithDefinedPrompt(UUID conversationId, String question, ResponseLevelDto responseLevel);

    ExpertAnswerResponseDto askWithoutPrompt(UUID conversationId, String question);
}

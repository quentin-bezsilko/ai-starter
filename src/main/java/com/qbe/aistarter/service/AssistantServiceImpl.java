package com.qbe.aistarter.service;

import com.qbe.aistarter.dto.ExpertAnswerResponseDto;
import com.qbe.aistarter.enums.ResponseLevelDto;
import com.qbe.aistarter.loader.PromptLoaderImpl;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssistantServiceImpl implements AssistantService {

    private final ChatClient chatClient;
    private final PromptLoaderImpl promptLoader;
    private final MessageChatMemoryAdvisor chatMemoryAdvisor;
    private final ConversationMemoryService conversationMemoryService;

    @Override
    public ExpertAnswerResponseDto askWithDefinedPrompt(
            UUID conversationId, String question, ResponseLevelDto responseLevel) {
        ResponseLevelDto effectiveLevel = responseLevel != null ? responseLevel : ResponseLevelDto.EXPERT;
        return processQuestion(conversationId, question, promptLoader.loadPrompt(effectiveLevel));
    }

    @Override
    public ExpertAnswerResponseDto askWithoutPrompt(UUID conversationId, String question) {
        return processQuestion(conversationId, question, null);
    }

    private ExpertAnswerResponseDto processQuestion(UUID conversationId, String question, String systemPrompt) {
        UUID effectiveConversationId = conversationMemoryService.initializeConversation(conversationId);
        conversationMemoryService.saveUserMessage(effectiveConversationId, question);

        var prompt = chatClient.prompt();
        if (systemPrompt != null) {
            prompt.system(systemPrompt);
        }

        String answer = prompt.user(question)
                .advisors(advisor -> advisor.advisors(chatMemoryAdvisor)
                        .param(ChatMemory.CONVERSATION_ID, effectiveConversationId.toString()))
                .call()
                .content();

        conversationMemoryService.saveAssistantMessage(effectiveConversationId, answer);
        return new ExpertAnswerResponseDto(effectiveConversationId, answer, Instant.now());
    }
}

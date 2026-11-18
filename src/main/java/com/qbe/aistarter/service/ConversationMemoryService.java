package com.qbe.aistarter.memory;

import java.util.UUID;

public interface ConversationMemoryService {

    UUID initializeConversation(UUID conversationId);

    void saveUserMessage(UUID conversationId, String content);

    void saveAssistantMessage(UUID conversationId, String content);
}

package com.qbe.aistarter.service;

import com.qbe.aistarter.entity.ConversationEntity;
import com.qbe.aistarter.entity.MessageEntity;
import com.qbe.aistarter.enums.MessageRole;
import com.qbe.aistarter.repository.ConversationRepository;
import com.qbe.aistarter.repository.MessageRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConversationMemoryServiceImpl implements ConversationMemoryService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Override
    public UUID initializeConversation(UUID conversationId) {
        UUID id = conversationId != null ? conversationId : UUID.randomUUID();
        conversationRepository
                .findById(id)
                .orElseGet(() -> conversationRepository.save(new ConversationEntity(id, Instant.now(), Instant.now())));
        return id;
    }

    @Override
    public void saveUserMessage(UUID conversationId, String content) {
        messageRepository.save(
                new MessageEntity(UUID.randomUUID(), conversationId, MessageRole.USER, content, Instant.now()));
    }

    @Override
    public void saveAssistantMessage(UUID conversationId, String content) {
        messageRepository.save(
                new MessageEntity(UUID.randomUUID(), conversationId, MessageRole.ASSISTANT, content, Instant.now()));
    }
}

package com.qbe.aistarter.entity;

import com.qbe.aistarter.enums.MessageRole;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "message",
        indexes = {
            @Index(name = "idx_message_conversation_id", columnList = "conversation_id"),
            @Index(name = "idx_message_created_at", columnList = "created_at"),
            @Index(name = "idx_message_conversation_created", columnList = "conversation_id, created_at")
        })
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private MessageRole role;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}

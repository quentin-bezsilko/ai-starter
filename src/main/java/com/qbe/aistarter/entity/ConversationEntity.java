package com.qbe.aistarter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Entity
@Table(name = "conversation")
@RequiredArgsConstructor
@AllArgsConstructor
public class ConversationEntity {

    @Id
    private UUID id;

    private Instant createdAt;

    private Instant updatedAt;
}

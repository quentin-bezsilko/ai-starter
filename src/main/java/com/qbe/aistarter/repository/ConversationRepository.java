package com.qbe.aistarter.repository;

import com.qbe.aistarter.entity.ConversationEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<ConversationEntity, UUID> {}

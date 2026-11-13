package com.qbe.aistarter.repository;

import com.qbe.aistarter.entity.TravelPlanEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelPlanRepository extends JpaRepository<TravelPlanEntity, UUID> {

    Optional<TravelPlanEntity> findByConversationId(String conversationId);

    List<TravelPlanEntity> findByDestinationContainingIgnoreCase(String destination);

    Optional<TravelPlanEntity> findTopByOrderByCreationDateDesc();
}

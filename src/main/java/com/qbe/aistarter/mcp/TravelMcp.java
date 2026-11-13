package com.qbe.aistarter.mcp;

import com.qbe.aistarter.entity.TravelPlanEntity;
import com.qbe.aistarter.repository.TravelPlanRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * Pour un MCP classique avec Spring AI, ces méthodes ne sont jamais appelées directement.
 * Les méthodes sont appelées sont LLM lorsqu'il décide qu'elles sont utiles.
 * => Donne au modèle des outils pour qu'il raisonne,
 * Consulte des données stockées, retrouve des voyages précédents et génère des livrables PDF,
 * au lieu de simplement produire du texte.
 */
@Component
@RequiredArgsConstructor
public class TravelMcp {

    private final TravelPlanRepository repository;

    @Tool(description = "Retrieve a travel plan by conversation id")
    public String getTravelPlan(String conversationId) {
        return repository
                .findByConversationId(conversationId)
                .map(TravelPlanEntity::getResponseJson)
                .orElse("No travel plan found");
    }

    @Tool(description = "Retrieve all travel destinations")
    public List<String> getDestinations() {
        return repository.findAll().stream()
                .map(TravelPlanEntity::getDestination)
                .distinct()
                .toList();
    }

    @Tool(description = "Count generated travel plans")
    public long countTravelPlans() {
        return repository.count();
    }

    @Tool(description = "Retrieve the last generated travel plan")
    public String getLastTravelPlan() {
        return repository
                .findTopByOrderByCreationDateDesc()
                .map(TravelPlanEntity::getResponseJson)
                .orElse("No travel plan found");
    }
}

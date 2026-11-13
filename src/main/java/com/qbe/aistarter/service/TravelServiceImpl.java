package com.qbe.aistarter.service;

import com.qbe.aistarter.dto.TravelRequestDto;
import com.qbe.aistarter.dto.TravelResponseDto;
import com.qbe.aistarter.loader.PromptLoader;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TravelServiceImpl {

    private final ChatClient chatClient;
    private final PromptLoader promptLoader;
    private final TravelPersistenceServiceImpl travelPersistenceService;
    // private final VectorStore vectorStore;

    private final Map<String, TravelResponseDto> travels = new ConcurrentHashMap<>();

    public TravelResponseDto generateTravelPlan(TravelRequestDto request) {
        String templateContent = promptLoader.load("prompts/travel-plan.st");
        /*List<Document> documents = vectorStore.similaritySearch(
        SearchRequest.builder().query(request.destination()).topK(3).build());*/

        /*String knowledge = documents.stream().map(Document::getText).reduce("", (a, b) -> a + "\n\n" + b);*/

        PromptTemplate template =
                PromptTemplate.builder().template(templateContent).build();

        String prompt = template.render(Map.of(
                "destination", request.destination(),
                "duration", request.durationInDays(),
                "budget", request.budgetInEuros(),
                "interests", String.join(", ", request.interests()),
                "profile", request.profile().description()));
        //   "knowledge", knowledge));

        TravelResponseDto response = chatClient
                .prompt()
                .user(prompt)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, request.conversationId()))
                .call()
                .entity(TravelResponseDto.class);

        travels.put(request.conversationId(), response);
        travelPersistenceService.save(request, response);

        return response;
    }

    public TravelResponseDto loadTrip(String conversationId) {
        return Optional.ofNullable(travels.get(conversationId))
                .orElseThrow(() -> new IllegalArgumentException("No trip found for conversationId: " + conversationId));
    }

    public String chat(String conversationId, String message) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }
}

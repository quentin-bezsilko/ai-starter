package com.qbe.aistarter.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class OllamaConfig {

    private static final String DEFAULT_SYSTEM_PROMPT =
            """
            Tu es un assistant technique et pédagogique précis, rigoureux et concis.
            Règles de réponse :
            1. Appuie-toi en priorité sur les informations fournies dans le contexte documentaire (RAG) et sur les résultats des outils disponibles.
            2. Réponds directement et concrètement à la question posée, sans formules d'introduction creuses.
            3. Si l'information demandée ne figure pas dans le contexte fourni, dis-le clairement plutôt que de deviner ou d'inventer.
            4. Lorsque tu utilises des données issues d'un document ou d'un outil, cite les faits, valeurs ou étapes exactes de manière structurée et claire.
            5. Réponds en français, sauf si l'utilisateur demande explicitement une autre langue.
            """;

    private static final int DEFAULT_TOP_K = 6;
    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.35;

    @Bean
    ChatClient chatClient(
            ChatClient.Builder chatClientBuilder, ToolCallbackProvider toolCallbackProvider, VectorStore vectorStore) {

        log.debug(
                "Configuring ChatClient with QuestionAnswerAdvisor (topK={}, similarityThreshold={})",
                DEFAULT_TOP_K,
                DEFAULT_SIMILARITY_THRESHOLD);

        var qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .topK(DEFAULT_TOP_K)
                        .similarityThreshold(DEFAULT_SIMILARITY_THRESHOLD)
                        .build())
                .build();

        return chatClientBuilder
                .defaultSystem(DEFAULT_SYSTEM_PROMPT)
                .defaultTools(toolCallbackProvider)
                .defaultAdvisors(qaAdvisor)
                .build();
    }
}

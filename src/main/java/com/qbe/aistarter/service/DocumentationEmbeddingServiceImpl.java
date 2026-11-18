package com.qbe.aistarter.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentationEmbeddingServiceImpl implements DocumentationEmbeddingService {

    private final EmbeddingModel embeddingModel;

    @Override
    public float[] generateEmbedding(String content) {
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("Content cannot be empty");
        }

        float[] embedding = embeddingModel.embed(content);
        validateEmbedding(embedding);
        return embedding;
    }

    @Override
    public List<float[]> generateEmbeddings(List<String> contents) {
        if (contents == null || contents.isEmpty()) {
            return List.of();
        }

        if (contents.stream().anyMatch(content -> !StringUtils.hasText(content))) {
            throw new IllegalArgumentException("Embedding contents cannot contain empty values");
        }

        log.info("Generating embeddings for {} chunks", contents.size());

        List<float[]> embeddings = embeddingModel.embed(contents);
        if (embeddings.size() != contents.size()) {
            throw new IllegalStateException("Embedding count does not match input count");
        }

        embeddings.forEach(this::validateEmbedding);
        log.info("Successfully generated {} embeddings", embeddings.size());
        return embeddings;
    }

    private void validateEmbedding(float[] embedding) {

        if (embedding == null || embedding.length == 0) {
            throw new IllegalStateException("Generated embedding is empty");
        }

        for (float value : embedding) {
            if (!Float.isFinite(value)) {
                throw new IllegalStateException("Embedding contains an invalid numerical value");
            }
        }
    }
}

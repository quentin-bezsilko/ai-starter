package com.qbe.aistarter.service;

import java.util.List;

public interface DocumentationEmbeddingService {

    float[] generateEmbedding(String content);

    List<float[]> generateEmbeddings(List<String> contents);
}

package com.qbe.aistarter.service;

import com.qbe.aistarter.enums.ResponseLevelDto;

public interface DocumentationRagService {

    RagResponse generateAnswer(String question, ResponseLevelDto responseLevel);

    record RagResponse(String answer, java.util.List<RagSource> sources) {}

    record RagSource(java.util.UUID documentId, String title, String url, int chunkIndex, double similarity) {}
}

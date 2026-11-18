package com.qbe.aistarter.service;

import com.qbe.aistarter.entity.DocumentationSourceEntity;
import com.qbe.aistarter.enums.DocumentationStatus;
import com.qbe.aistarter.reader.DocumentationContentReader;
import com.qbe.aistarter.reader.DocumentationContentReader.ExtractedDocument;
import com.qbe.aistarter.repository.DocumentationSourceRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class DocumentationIngestionServiceImpl {

    private final DocumentationSourceRepository sourceRepository;
    private final List<DocumentationContentReader> contentReaders;
    private final DocumentChunkingServiceImpl chunkingService;
    private final DocumentationEmbeddingService embeddingService;
    private final DocumentationDocumentPersistenceService persistenceService;

    public DocumentationIngestionServiceImpl(
            DocumentationSourceRepository sourceRepository,
            List<DocumentationContentReader> contentReaders,
            DocumentChunkingServiceImpl chunkingService,
            DocumentationEmbeddingService embeddingService,
            DocumentationDocumentPersistenceService persistenceService) {
        this.sourceRepository = sourceRepository;
        this.contentReaders = contentReaders;
        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
        this.persistenceService = persistenceService;
    }

    public void ingest(UUID sourceId) {
        DocumentationSourceEntity source = sourceRepository
                .findByIdAndEnabledTrue(sourceId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Documentation source not found or disabled: " + sourceId));

        try {
            markSourceAsProcessing(source);
            ingestSource(source);
            markSourceAsSuccess(source);
        } catch (Exception exception) {
            log.error("Documentation ingestion failed for source {}", sourceId, exception);
            markSourceAsFailed(source, exception);
            throw new IllegalStateException("Unable to ingest documentation source: " + sourceId, exception);
        }
    }

    private void ingestSource(DocumentationSourceEntity source) {
        DocumentationContentReader reader = contentReaders.stream()
                .filter(candidate -> candidate.supports(source))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("No reader supports documentation source: " + source.getUrl()));

        List<ExtractedDocument> documents = reader.read(source);
        if (documents.isEmpty()) {
            throw new IllegalStateException("No documents were extracted from source: " + source.getUrl());
        }

        for (ExtractedDocument document : documents) {
            ingestDocument(source, document);
        }
    }

    private void ingestDocument(DocumentationSourceEntity source, ExtractedDocument extractedDocument) {
        if (!StringUtils.hasText(extractedDocument.content())) {
            throw new IllegalStateException("Extracted document content is empty: " + extractedDocument.canonicalUrl());
        }

        String contentHash = calculateSha256(extractedDocument.content());
        if (persistenceService.exists(source.getId(), extractedDocument.canonicalUrl(), contentHash)) {
            log.info("Document already ingested: {}", extractedDocument.canonicalUrl());
            return;
        }

        List<String> chunks = chunkingService.splitContent(extractedDocument.content());
        if (chunks.isEmpty()) {
            throw new IllegalStateException("No chunks generated for document: " + extractedDocument.canonicalUrl());
        }

        List<float[]> embeddings = embeddingService.generateEmbeddings(chunks);
        if (embeddings.size() != chunks.size()) {
            throw new IllegalStateException("Embedding count does not match chunk count");
        }
        persistenceService.save(source, extractedDocument, contentHash, chunks, embeddings);
    }

    private void markSourceAsProcessing(DocumentationSourceEntity source) {
        source.setStatus(DocumentationStatus.PROCESSING);
        source.setLastError(null);
        sourceRepository.save(source);
    }

    private void markSourceAsSuccess(DocumentationSourceEntity source) {
        source.setStatus(DocumentationStatus.SUCCESS);
        source.setLastCrawledAt(Instant.now());
        source.setLastSuccessAt(Instant.now());
        source.setLastError(null);

        sourceRepository.save(source);
    }

    private void markSourceAsFailed(DocumentationSourceEntity source, Exception exception) {
        source.setStatus(DocumentationStatus.FAILED);
        source.setLastCrawledAt(Instant.now());
        source.setLastError(exception.getMessage());

        sourceRepository.save(source);
    }

    private String calculateSha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to calculate document hash", exception);
        }
    }

    public void ingestAll() {
        List<DocumentationSourceEntity> sources = sourceRepository.findAllByEnabledTrue();
        log.info("Starting ingestion of {} sources", sources.size());

        for (DocumentationSourceEntity source : sources) {
            try {
                ingest(source.getId());
            } catch (Exception exception) {
                log.error("Failed to ingest source {}", source.getId(), exception);
            }
        }
        log.info("Documentation ingestion completed");
    }
}

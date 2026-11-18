package com.qbe.aistarter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qbe.aistarter.entity.DocumentationChunkEntity;
import com.qbe.aistarter.entity.DocumentationDocumentEntity;
import com.qbe.aistarter.entity.DocumentationSourceEntity;
import com.qbe.aistarter.enums.DocumentationStatus;
import com.qbe.aistarter.reader.DocumentationContentReader;
import com.qbe.aistarter.reader.DocumentationContentReader.ExtractedDocument;
import com.qbe.aistarter.repository.DocumentationDocumentRepository;
import com.qbe.aistarter.repository.DocumentationSourceRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentationIngestionServiceImpl {

    private final DocumentationSourceRepository sourceRepository;
    private final DocumentationDocumentRepository documentRepository;
    private final DocumentationContentReader contentReader;
    private final DocumentChunkingServiceImpl chunkingService;
    private final DocumentationEmbeddingService embeddingService;
    private final ObjectMapper objectMapper;

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

        ExtractedDocument extractedDocument = contentReader.read(source);

        String content = extractedDocument.content();

        if (content == null || content.isBlank()) {
            throw new IllegalStateException("Extracted document content is empty");
        }

        String contentHash = calculateSha256(content);

        boolean alreadyIngested = documentRepository.existsBySourceIdAndCanonicalUrlAndContentHash(
                source.getId(), extractedDocument.canonicalUrl(), contentHash);

        if (alreadyIngested) {
            log.info("Document already ingested: {}", extractedDocument.canonicalUrl());
            return;
        }

        saveDocument(source, extractedDocument, contentHash);
    }

    @Transactional
    protected void saveDocument(
            DocumentationSourceEntity source, ExtractedDocument extractedDocument, String contentHash) {

        DocumentationDocumentEntity document = new DocumentationDocumentEntity();

        document.setSource(source);
        document.setTitle(extractedDocument.title());
        document.setCanonicalUrl(extractedDocument.canonicalUrl());
        document.setContentType(extractedDocument.contentType());
        document.setVersion(source.getVersion());
        document.setContentHash(contentHash);
        document.setStatus(DocumentationStatus.PROCESSING);
        document.setMetadata("{}");

        document = documentRepository.save(document);

        List<String> chunks = chunkingService.splitContent(extractedDocument.content());

        if (chunks.isEmpty()) {
            throw new IllegalStateException("No chunks generated for document: " + document.getId());
        }

        List<float[]> embeddings = embeddingService.generateEmbeddings(chunks);

        if (embeddings.size() != chunks.size()) {
            throw new IllegalStateException("Embedding count does not match chunk count");
        }

        List<DocumentationChunkEntity> entities = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {

            DocumentationChunkEntity chunk = new DocumentationChunkEntity();

            chunk.setDocument(document);
            chunk.setChunkIndex(i);
            chunk.setContent(chunks.get(i));
            chunk.setEmbedding(new com.pgvector.PGvector(embeddings.get(i)));
            chunk.setMetadata("{}");

            entities.add(chunk);
        }

        document.getChunks().addAll(entities);
        document.setStatus(DocumentationStatus.SUCCESS);
        document.setIngestedAt(Instant.now());

        documentRepository.save(document);

        log.info("Document successfully ingested: {} ({} chunks)", document.getId(), chunks.size());
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

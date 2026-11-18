package com.qbe.aistarter.service;

import com.pgvector.PGvector;
import com.qbe.aistarter.entity.DocumentationChunkEntity;
import com.qbe.aistarter.entity.DocumentationDocumentEntity;
import com.qbe.aistarter.entity.DocumentationSourceEntity;
import com.qbe.aistarter.enums.DocumentationStatus;
import com.qbe.aistarter.reader.DocumentationContentReader.ExtractedDocument;
import com.qbe.aistarter.repository.DocumentationDocumentRepository;
import com.qbe.aistarter.repository.DocumentationSourceRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentationDocumentPersistenceService {

    private final DocumentationDocumentRepository documentRepository;
    private final DocumentationSourceRepository sourceRepository;

    public boolean exists(UUID sourceId, String canonicalUrl, String contentHash) {
        return documentRepository.existsBySourceIdAndCanonicalUrlAndContentHash(sourceId, canonicalUrl, contentHash);
    }

    @Transactional
    public void save(
            DocumentationSourceEntity source,
            ExtractedDocument extractedDocument,
            String contentHash,
            List<String> contents,
            List<float[]> embeddings) {
        if (contents.size() != embeddings.size()) {
            throw new IllegalArgumentException("Embedding count does not match chunk count");
        }

        DocumentationSourceEntity managedSource = sourceRepository.getReferenceById(source.getId());
        DocumentationDocumentEntity document = new DocumentationDocumentEntity();
        document.setSource(managedSource);
        document.setTitle(extractedDocument.title());
        document.setCanonicalUrl(extractedDocument.canonicalUrl());
        document.setContentType(extractedDocument.contentType());
        document.setVersion(managedSource.getVersion());
        document.setContentHash(contentHash);
        document.setStatus(DocumentationStatus.PROCESSING);
        document.setMetadata("{}");

        List<DocumentationChunkEntity> chunks = new ArrayList<>();
        for (int i = 0; i < contents.size(); i++) {
            DocumentationChunkEntity chunk = new DocumentationChunkEntity();
            chunk.setDocument(document);
            chunk.setChunkIndex(i);
            chunk.setContent(contents.get(i));
            chunk.setEmbedding(new PGvector(embeddings.get(i)));
            chunk.setMetadata("{}");
            chunks.add(chunk);
        }

        document.getChunks().addAll(chunks);
        document.setStatus(DocumentationStatus.SUCCESS);
        document.setIngestedAt(Instant.now());
        documentRepository.save(document);

        log.info("Document successfully ingested: {} ({} chunks)", document.getId(), chunks.size());
    }
}

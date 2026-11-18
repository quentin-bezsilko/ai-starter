package com.qbe.aistarter.repository;

import com.qbe.aistarter.entity.DocumentationDocumentEntity;
import com.qbe.aistarter.enums.DocumentationStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentationDocumentRepository extends JpaRepository<DocumentationDocumentEntity, UUID> {

    List<DocumentationDocumentEntity> findAllBySourceId(UUID sourceId);

    List<DocumentationDocumentEntity> findAllBySourceIdAndStatus(UUID sourceId, DocumentationStatus status);

    Optional<DocumentationDocumentEntity> findBySourceIdAndCanonicalUrlAndContentHash(
            UUID sourceId, String canonicalUrl, String contentHash);

    boolean existsBySourceIdAndCanonicalUrlAndContentHash(UUID sourceId, String canonicalUrl, String contentHash);

    Optional<DocumentationDocumentEntity> findFirstBySourceIdAndCanonicalUrlOrderByCreatedAtDesc(
            UUID sourceId, String canonicalUrl);

    long countBySourceId(UUID sourceId);
}

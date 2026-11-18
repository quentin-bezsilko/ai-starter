package com.qbe.aistarter.entity;

import com.qbe.aistarter.enums.DocumentationStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
        name = "documentation_document",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_documentation_document_content",
                    columnNames = {"source_id", "canonical_url", "content_hash"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentationDocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private DocumentationSourceEntity source;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "canonical_url", nullable = false, columnDefinition = "TEXT")
    private String canonicalUrl;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(length = 50)
    private String version;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentationStatus status = DocumentationStatus.PENDING;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String metadata = "{}";

    @Column(name = "ingested_at")
    private Instant ingestedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("chunkIndex ASC")
    private List<DocumentationChunkEntity> chunks = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}

package com.qbe.aistarter.entity;

import com.qbe.aistarter.enums.DocumentationSourceType;
import com.qbe.aistarter.enums.DocumentationStatus;
import com.qbe.aistarter.enums.DocumentationTechnology;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "documentation_source")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentationSourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DocumentationTechnology technology;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private DocumentationSourceType sourceType = DocumentationSourceType.WEB;

    @Column(columnDefinition = "TEXT")
    private String url;

    @Column(name = "allowed_host", length = 255)
    private String allowedHost;

    @Column(length = 50)
    private String version;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "crawl_enabled", nullable = false)
    private boolean crawlEnabled = false;

    @Column(name = "max_depth", nullable = false)
    private Integer maxDepth = 1;

    @Column(name = "crawl_frequency_hours", nullable = false)
    private Integer crawlFrequencyHours = 168;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentationStatus status = DocumentationStatus.PENDING;

    @Column(name = "last_crawled_at")
    private Instant lastCrawledAt;

    @Column(name = "last_success_at")
    private Instant lastSuccessAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "source", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentationDocumentEntity> documents = new ArrayList<>();

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

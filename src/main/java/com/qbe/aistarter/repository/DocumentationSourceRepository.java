package com.qbe.aistarter.repository;

import com.qbe.aistarter.entity.DocumentationSourceEntity;
import com.qbe.aistarter.enums.DocumentationStatus;
import com.qbe.aistarter.enums.DocumentationTechnology;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentationSourceRepository extends JpaRepository<DocumentationSourceEntity, UUID> {

    List<DocumentationSourceEntity> findAllByEnabledTrue();

    List<DocumentationSourceEntity> findAllByEnabledTrueAndCrawlEnabledTrue();

    List<DocumentationSourceEntity> findAllByStatus(DocumentationStatus status);

    List<DocumentationSourceEntity> findAllByTechnologyAndEnabledTrue(DocumentationTechnology technology);

    Optional<DocumentationSourceEntity> findByIdAndEnabledTrue(UUID id);
}

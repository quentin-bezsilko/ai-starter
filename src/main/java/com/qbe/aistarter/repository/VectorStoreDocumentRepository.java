package com.qbe.aistarter.repository;

import com.qbe.aistarter.entity.VectorStoreDocumentEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VectorStoreDocumentRepository extends JpaRepository<VectorStoreDocumentEntity, UUID> {

    @Query(
            value =
                    """
                    select metadata->>'file_name' as file_name, count(*) as chunks_count
                    from vector_store
                    where metadata->>'file_name' is not null
                    group by metadata->>'file_name'
                    order by file_name
                    """,
            nativeQuery = true)
    List<IndexedDocumentProjection> findIndexedDocumentsSummary();

    interface IndexedDocumentProjection {
        String getFileName();

        Long getChunksCount();
    }
}

package com.qbe.aistarter.service;

import com.pgvector.PGvector;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentationRetrievalServiceImpl {

    private static final int DEFAULT_TOP_K = 5;
    private static final double DEFAULT_MIN_SIMILARITY = 0.50;

    private final JdbcTemplate jdbcTemplate;
    private final DocumentationEmbeddingServiceImpl embeddingService;

    /**
     * Recherche les chunks les plus pertinents pour une question.
     */
    public List<RetrievedChunk> search(String question) {
        return search(question, DEFAULT_TOP_K, DEFAULT_MIN_SIMILARITY);
    }

    /**
     * Recherche avec un nombre de résultats et un seuil personnalisés.
     */
    public List<RetrievedChunk> search(String question, int topK, double minSimilarity) {
        if (!StringUtils.hasText(question)) {
            throw new IllegalArgumentException("Search question cannot be empty");
        }
        if (topK < 1 || topK > 100) {
            throw new IllegalArgumentException("topK must be between 1 and 100");
        }
        if (minSimilarity < -1 || minSimilarity > 1) {
            throw new IllegalArgumentException("Similarity must be between -1 and 1");
        }

        log.info("Searching documentation for: {}", question);

        // 1. Génération de l'embedding de la question
        float[] embedding = embeddingService.generateEmbedding(question);

        // 2. Recherche vectorielle
        List<RetrievedChunk> results = findSimilarChunks(embedding, topK, minSimilarity);
        log.info("Found {} relevant documentation chunks", results.size());
        return results;
    }

    /**
     * Exécute une recherche par similarité cosinus dans PgVector.
     */
    private List<RetrievedChunk> findSimilarChunks(float[] embedding, int topK, double minSimilarity) {
        String sql =
                """
                SELECT
                    c.id AS chunk_id,
                    c.document_id,
                    c.chunk_index,
                    c.content,
                    c.metadata,
                    d.title AS document_title,
                    d.canonical_url,
                    1 - (c.embedding <=> ?) AS similarity
                FROM documentation_chunk c
                JOIN documentation_document d
                    ON d.id = c.document_id
                WHERE c.embedding IS NOT NULL
                ORDER BY c.embedding <=> ?
                LIMIT ?
                """;

        List<RetrievedChunk> results = jdbcTemplate.query(
                sql,
                statement -> {
                    PGvector.registerTypes(statement.getConnection());

                    PGvector vector = new PGvector(embedding);

                    statement.setObject(1, vector);
                    statement.setObject(2, vector);
                    statement.setInt(3, topK);
                },
                (rs, rowNum) -> mapRetrievedChunk(rs));

        return results.stream()
                .filter(result -> result.similarity() >= minSimilarity)
                .toList();
    }

    private RetrievedChunk mapRetrievedChunk(ResultSet rs) throws SQLException {
        return new RetrievedChunk(
                rs.getObject("chunk_id", UUID.class),
                rs.getObject("document_id", UUID.class),
                rs.getInt("chunk_index"),
                rs.getString("content"),
                rs.getString("document_title"),
                rs.getString("canonical_url"),
                rs.getDouble("similarity"),
                rs.getString("metadata"));
    }

    /**
     * Résultat d'une recherche documentaire.
     */
    public record RetrievedChunk(
            UUID chunkId,
            UUID documentId,
            int chunkIndex,
            String content,
            String documentTitle,
            String canonicalUrl,
            double similarity,
            String metadata) {}
}

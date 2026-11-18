package com.qbe.aistarter.service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DocumentChunkingServiceImpl {

    private static final int CHUNK_SIZE = 800;
    private static final int MIN_CHUNK_SIZE = 350;
    private static final int MIN_CHUNK_LENGTH_TO_EMBED = 5;
    private static final int MAX_NUM_CHUNKS = 10000;

    private final TokenTextSplitter textSplitter;

    public DocumentChunkingServiceImpl() {
        this.textSplitter = TokenTextSplitter.builder()
                .withChunkSize(CHUNK_SIZE)
                .withMinChunkSizeChars(MIN_CHUNK_SIZE)
                .withMinChunkLengthToEmbed(MIN_CHUNK_LENGTH_TO_EMBED)
                .withMaxNumChunks(MAX_NUM_CHUNKS)
                .withKeepSeparator(true)
                .build();
    }

    /**
     * Découpe un document Spring AI en plusieurs documents.
     */
    public List<Document> split(Document document) {
        List<Document> chunks = textSplitter.apply(List.of(document));
        log.debug("Document split into {} chunks", chunks.size());
        return chunks;
    }

    /**
     * Découpe un contenu textuel en plusieurs fragments.
     */
    public List<String> splitContent(String content) {
        if (content == null || content.isBlank()) {
            return List.of();
        }

        Document document = new Document(content);
        return split(document).stream().map(Document::getText).toList();
    }
}

package com.qbe.aistarter.loader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RagDataLoader {

    /*private final VectorStore vectorStore;

    @PostConstruct
    public void loadDocuments() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:rag/*.txt");
        List<Document> documents = new ArrayList<>();
        for (Resource resource : resources) {
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            Document document = Document.builder()
                    .text(content)
                    .metadata("source", resource.getFilename())
                    .build();
            documents.add(document);
            log.info("Loaded {}", resource.getFilename());
        }

        vectorStore.add(documents);
        log.info("{} documents indexed into VectorStore", documents.size());
    }*/
}

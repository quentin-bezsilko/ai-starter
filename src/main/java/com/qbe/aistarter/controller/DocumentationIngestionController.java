package com.qbe.aistarter.controller;

import com.qbe.aistarter.entity.DocumentationSourceEntity;
import com.qbe.aistarter.repository.DocumentationSourceRepository;
import com.qbe.aistarter.service.DocumentationIngestionServiceImpl;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/documentation")
@RequiredArgsConstructor
public class DocumentationIngestionController {

    private final DocumentationIngestionServiceImpl ingestionService;
    private final DocumentationSourceRepository sourceRepository;

    @PostMapping("/sources/{id}/ingest")
    public ResponseEntity<Void> ingest(@PathVariable UUID id) {
        ingestionService.ingest(id);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/ingest-all")
    public ResponseEntity<Void> ingestAll() {
        ingestionService.ingestAll();
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/sources")
    public ResponseEntity<List<DocumentationSourceEntity>> getSources() {
        return ResponseEntity.ok(sourceRepository.findAll());
    }
}

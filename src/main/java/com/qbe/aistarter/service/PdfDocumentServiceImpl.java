package com.qbe.aistarter.service;

import com.qbe.aistarter.dto.IndexedDocumentDto;
import com.qbe.aistarter.repository.VectorStoreDocumentRepository;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfDocumentServiceImpl implements PdfDocumentService {

    private static final String DEFAULT_FILENAME = "document.pdf";
    private static final String FILE_NAME_METADATA_KEY = "file_name";

    private final VectorStore vectorStore;
    private final VectorStoreDocumentRepository vectorStoreDocumentRepository;

    @Override
    public void deletePdf(String filename) {
        Objects.requireNonNull(filename, "filename must not be null");
        log.debug("Deleting vectors for file '{}'", filename);

        var filterExpression = new FilterExpressionBuilder()
                .eq(FILE_NAME_METADATA_KEY, filename)
                .build();
        vectorStore.delete(filterExpression);
    }

    @Transactional
    @Override
    public int ingestOrUpdatePdf(MultipartFile file) throws IOException {
        Objects.requireNonNull(file, "file must not be null");
        String filename = file.getOriginalFilename() != null
                        && !file.getOriginalFilename().isBlank()
                ? file.getOriginalFilename().trim()
                : DEFAULT_FILENAME;

        log.info("Ingesting or updating PDF '{}'", filename);

        deletePdf(filename);

        var resource = new InputStreamResource(file.getInputStream());
        var pdfReader = new PagePdfDocumentReader(resource);
        var splitter = TokenTextSplitter.builder().build();

        List<Document> chunks = splitter.apply(pdfReader.get());
        chunks.forEach(doc -> doc.getMetadata().put(FILE_NAME_METADATA_KEY, filename));

        vectorStore.add(chunks);
        log.info("Indexed {} chunks for file '{}'", chunks.size(), filename);
        return chunks.size();
    }

    @Override
    public List<IndexedDocumentDto> listIndexedDocuments() {
        log.debug("Listing indexed documents from repository");
        return vectorStoreDocumentRepository.findIndexedDocumentsSummary().stream()
                .map(row -> new IndexedDocumentDto(row.getFileName(), row.getChunksCount()))
                .toList();
    }
}

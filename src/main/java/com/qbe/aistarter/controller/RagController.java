package com.qbe.aistarter.controller;

import com.qbe.aistarter.dto.IndexedDocumentDto;
import com.qbe.aistarter.dto.RagResponseDto;
import com.qbe.aistarter.dto.SourceInfoDto;
import com.qbe.aistarter.service.PdfDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/rag", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "RAG", description = "Indexed PDF document management and RAG requests")
public class RagController {

    private static final int EXCERPT_MAX_LENGTH = 150;
    private static final String PDF_MAGIC_HEADER = "%PDF-";

    private final ChatClient chatClient;
    private final PdfDocumentService ingestionService;

    @GetMapping("/documents")
    @Operation(
            summary = "List indexed documents",
            description = "Returns the list of PDF documents present in the vector index.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "List retrieved successfully",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                array = @ArraySchema(schema = @Schema(implementation = IndexedDocumentDto.class)))),
        @ApiResponse(responseCode = "500", description = "Error while reading the index", content = @Content)
    })
    public ResponseEntity<List<IndexedDocumentDto>> listDocuments() {
        log.debug("Retrieving the list of indexed RAG documents");
        List<IndexedDocumentDto> documents = ingestionService.listIndexedDocuments();
        return ResponseEntity.ok(documents);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Index or update a PDF",
            description = "Validates a PDF file and then starts its ingestion and vector indexing.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "PDF file indexed successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(
                responseCode = "400",
                description = "File missing, empty, or invalid",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(
                responseCode = "500",
                description = "Internal error during ingestion or embedding computation",
                content = @Content)
    })
    public ResponseEntity<Map<String, Object>> uploadPdf(
            @Parameter(
                            description = "PDF file to index",
                            required = true,
                            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
                    @RequestPart("file")
                    @NotNull
                    MultipartFile file)
            throws IOException {

        if (!isValidPdf(file)) {
            log.warn(
                    "Upload rejected: invalid file (originalFilename={}, contentType={}, size={})",
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize());
            return ResponseEntity.badRequest().body(Map.of("error", "Please provide a valid PDF file."));
        }

        String originalFilename = Objects.toString(file.getOriginalFilename(), "document.pdf");
        log.info("Starting ingestion of PDF '{}' ({} bytes)", originalFilename, file.getSize());

        int chunksCount = ingestionService.ingestOrUpdatePdf(file);

        log.info("Finished ingestion of PDF '{}' : {} chunk(s) indexed", originalFilename, chunksCount);
        return ResponseEntity.ok(Map.of(
                "message", "File indexed successfully",
                "fileName", originalFilename,
                "chunksIndexed", chunksCount));
    }

    @GetMapping("/ask")
    @Operation(
            summary = "Ask a RAG question",
            description = "Queries the model using the vector store, filterable by file name.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "RAG response generated successfully",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = RagResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Missing or empty question parameter", content = @Content),
        @ApiResponse(
                responseCode = "500",
                description = "Error during vector search or LLM generation",
                content = @Content)
    })
    public ResponseEntity<RagResponseDto> ask(
            @Parameter(description = "Question to ask the indexed documents", required = true)
                    @RequestParam("question")
                    @NotBlank
                    String question,
            @Parameter(description = "Exact file name to filter the vector search")
                    @RequestParam(value = "fileName", required = false)
                    String fileName) {

        String trimmedQuestion = question.trim();
        String normalizedFileName = (fileName != null && !fileName.isBlank()) ? fileName.trim() : null;

        log.debug("Received RAG question (fileNameFilter={})", normalizedFileName);

        var promptSpec = chatClient.prompt().user(trimmedQuestion);

        if (normalizedFileName != null) {
            String escapedFileName = escapeFilterLiteral(normalizedFileName);
            promptSpec.advisors(advisorSpec -> advisorSpec.param(
                    QuestionAnswerAdvisor.FILTER_EXPRESSION, "file_name == '" + escapedFileName + "'"));
        }

        ChatResponse response = promptSpec.call().chatResponse();

        String answer = extractAnswerText(response);
        List<SourceInfoDto> sources = extractSources(response);

        return ResponseEntity.ok(new RagResponseDto(answer, sources));
    }

    @DeleteMapping("/documents/{fileName}")
    @Operation(
            summary = "Delete an indexed document",
            description = "Deletes all vector entries associated with the provided file name.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Document deleted from the vector index",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "400", description = "Invalid file name", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error during deletion from the database", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> deletePdf(
            @Parameter(description = "Name of the file to delete from the index", required = true)
                    @PathVariable("fileName")
                    @NotBlank
                    String fileName) {

        String targetFileName = fileName.trim();
        log.info("Suppression du document '{}' de l'index vectoriel", targetFileName);

        ingestionService.deletePdf(targetFileName);

        return ResponseEntity.ok(
                Map.of("message", "Document supprimé de l'index vectoriel", "fileName", targetFileName));
    }

    private boolean isValidPdf(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        boolean hasPdfMime = (MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(contentType)
                || MediaType.APPLICATION_OCTET_STREAM_VALUE.equalsIgnoreCase(contentType));

        String filename = file.getOriginalFilename();
        boolean hasPdfExtension =
                filename != null && filename.trim().toLowerCase(Locale.ROOT).endsWith(".pdf");
        boolean hasPdfSignature = hasPdfHeader(file);

        return hasPdfSignature && (hasPdfMime || hasPdfExtension || contentType == null);
    }

    private boolean hasPdfHeader(MultipartFile file) throws IOException {
        byte[] headerBytes = new byte[PDF_MAGIC_HEADER.length()];
        try (InputStream is = file.getInputStream()) {
            int read = is.readNBytes(headerBytes, 0, headerBytes.length);
            if (read < headerBytes.length) {
                return false;
            }
        }
        String signature = new String(headerBytes, StandardCharsets.US_ASCII);
        return PDF_MAGIC_HEADER.equals(signature);
    }

    /**
     * Échappe les antislashs et apostrophes pour éviter une syntaxe invalide dans l'expression de filtre.
     */
    private String escapeFilterLiteral(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    /**
     * Extrait en toute sécurité le texte généré par le modèle depuis {@link ChatResponse}.
     */
    private String extractAnswerText(ChatResponse response) {
        if (response == null
                || response.getResult() == null
                || response.getResult().getOutput().getText() == null) {
            return "";
        }
        return response.getResult().getOutput().getText();
    }

    private List<SourceInfoDto> extractSources(ChatResponse response) {
        if (response == null) {
            return List.of();
        }
        Object rawDocs = response.getMetadata().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
        if (!(rawDocs instanceof List<?> docList)) {
            return List.of();
        }

        return docList.stream()
                .filter(Document.class::isInstance)
                .map(Document.class::cast)
                .map(this::toSourceInfo)
                .toList();
    }

    private SourceInfoDto toSourceInfo(Document doc) {
        Map<String, Object> metadata = doc.getMetadata();
        String docFileName = String.valueOf(metadata.getOrDefault("file_name", "inconnu"));
        Object pageNumber = metadata.getOrDefault("page_number", "N/A");
        String text = Objects.toString(doc.getText(), "");
        String excerpt = text.substring(0, Math.min(text.length(), EXCERPT_MAX_LENGTH)) + "...";
        return new SourceInfoDto(docFileName, pageNumber, excerpt);
    }
}

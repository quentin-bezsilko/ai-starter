package com.qbe.aistarter.controller;

import com.qbe.aistarter.dto.AskQuestionRequestDto;
import com.qbe.aistarter.dto.ExpertAnswerResponseDto;
import com.qbe.aistarter.service.AssistantService;
import com.qbe.aistarter.service.DocumentationRagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assistant")
public class AssistantController {

    private final AssistantService assistantService;
    private final DocumentationRagService documentationRagService;

    @PostMapping("/question")
    public ResponseEntity<ExpertAnswerResponseDto> askWithDefinedPrompt(
            @Valid @RequestBody AskQuestionRequestDto request) {
        ExpertAnswerResponseDto response = assistantService.askWithDefinedPrompt(
                request.conversationId(), request.question(), request.responseLevel());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/question/simple")
    public ResponseEntity<ExpertAnswerResponseDto> askWithoutPrompt(@Valid @RequestBody AskQuestionRequestDto request) {
        ExpertAnswerResponseDto response =
                assistantService.askWithoutPrompt(request.conversationId(), request.question());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/question/rag")
    public ResponseEntity<DocumentationRagService.RagResponse> askWithRag(
            @Valid @RequestBody AskQuestionRequestDto request) {
        DocumentationRagService.RagResponse response =
                documentationRagService.generateAnswer(request.question(), request.responseLevel());
        return ResponseEntity.ok(response);
    }
}

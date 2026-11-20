package com.qbe.aistarter.controller;

import com.qbe.aistarter.dto.LlmResponseDto;
import com.qbe.aistarter.dto.PromptDto;
import com.qbe.aistarter.service.AssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assistant")
public class AssistantController {

    private final AssistantService assistantService;

    @PostMapping("/question")
    public ResponseEntity<LlmResponseDto> askWithPromptTemplate(@Valid @RequestBody PromptDto request) {
        LlmResponseDto response = assistantService.askWithPromptTemplate(
                request.conversationId(), request.question(), request.responseLevel());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/question/simple")
    public ResponseEntity<LlmResponseDto> askWithoutPromptTemplate(@Valid @RequestBody PromptDto request) {
        LlmResponseDto response =
                assistantService.askWithoutPromptTemplate(request.conversationId(), request.question());
        return ResponseEntity.ok(response);
    }
}

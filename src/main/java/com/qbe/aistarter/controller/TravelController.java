package com.qbe.aistarter.controller;

import com.qbe.aistarter.dto.TravelChatRequestDto;
import com.qbe.aistarter.dto.TravelRequestDto;
import com.qbe.aistarter.dto.TravelResponseDto;
import com.qbe.aistarter.service.PdfTravelServiceImpl;
import com.qbe.aistarter.service.TravelServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/travel")
@RequiredArgsConstructor
public class TravelController {

    private final TravelServiceImpl travelService;
    private final PdfTravelServiceImpl pdfTravelService;

    @PostMapping("/generate")
    public ResponseEntity<TravelResponseDto> generate(@Valid @RequestBody TravelRequestDto request) {
        TravelResponseDto plan = travelService.generateTravelPlan(request);
        return ResponseEntity.ok(plan);
    }

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody TravelChatRequestDto request) {
        String response = travelService.chat(request.conversationId(), request.message());
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/travel/{conversationId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getPdf(@PathVariable String conversationId) {
        TravelResponseDto travel = travelService.loadTrip(conversationId);
        byte[] pdf = pdfTravelService.generatePdf(travel);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=travel-plan.pdf")
                .body(pdf);
    }
}

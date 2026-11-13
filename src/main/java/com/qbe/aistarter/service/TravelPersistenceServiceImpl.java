package com.qbe.aistarter.service;

import com.qbe.aistarter.dto.TravelRequestDto;
import com.qbe.aistarter.dto.TravelResponseDto;
import com.qbe.aistarter.entity.TravelPlanEntity;
import com.qbe.aistarter.repository.TravelPlanRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Service
public class TravelPersistenceServiceImpl {

    private final TravelPlanRepository repository;
    private final ObjectMapper objectMapper;

    public void save(TravelRequestDto request, TravelResponseDto response) {
        repository.save(TravelPlanEntity.builder()
                .conversationId(request.conversationId())
                .destination(request.destination())
                .responseJson(objectMapper.writeValueAsString(response))
                .creationDate(Instant.now())
                .build());
    }
}

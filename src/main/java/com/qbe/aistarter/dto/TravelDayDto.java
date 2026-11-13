package com.qbe.aistarter.dto;

import java.util.List;

public record TravelDayDto(
        int dayNumber,
        String theme,
        List<ActivityDto> morning,
        List<ActivityDto> afternoon,
        List<ActivityDto> evening,
        double estimatedDailyCost) {}

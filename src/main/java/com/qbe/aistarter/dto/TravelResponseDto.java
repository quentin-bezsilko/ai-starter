package com.qbe.aistarter.dto;

import java.util.List;

public record TravelResponseDto(
        TravelSummaryDto summary,
        List<TravelDayDto> days,
        BudgetBreakdownDto budgetBreakdown,
        List<String> travelTips) {}

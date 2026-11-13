package com.qbe.aistarter.dto;

public record TravelSummaryDto(
        String destination, int durationInDays, double budgetInEuros, double estimatedTotalCost, String overview) {}

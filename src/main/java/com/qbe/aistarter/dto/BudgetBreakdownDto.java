package com.qbe.aistarter.dto;

public record BudgetBreakdownDto(
        double accommodation,
        double food,
        double transportation,
        double activities,
        double miscellaneous,
        double total) {}

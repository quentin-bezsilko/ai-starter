package com.qbe.aistarter.dto;

public record ActivityDto(
        String name, String description, String location, double estimatedCost, int durationMinutes, String reason) {}

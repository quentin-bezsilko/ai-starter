package com.qbe.aistarter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OpenMeteoClientResponseDto(Daily daily) {

    public record Daily(@JsonProperty("temperature_2m_mean") List<Double> temperatures) {}
}

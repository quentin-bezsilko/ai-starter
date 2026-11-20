package com.qbe.aistarter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenMeteoCurrentWeatherResponseDto(Current current) {

    public record Current(
            String time,
            @JsonProperty("temperature_2m") Double temperature,
            @JsonProperty("wind_speed_10m") Double windSpeed) {}
}

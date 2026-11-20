package com.qbe.aistarter.client;

import com.qbe.aistarter.dto.OpenMeteoClientResponseDto;
import com.qbe.aistarter.dto.OpenMeteoCurrentWeatherResponseDto;
import com.qbe.aistarter.dto.WeatherResponseDto;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Component
public class OpenMeteoClientImpl implements OpenMeteoClient {

    private final RestClient weatherRestClient;

    @Override
    public WeatherResponseDto getCurrentWeather(BigDecimal latitude, BigDecimal longitude, String city) {

        OpenMeteoCurrentWeatherResponseDto response = weatherRestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("current", "temperature_2m,wind_speed_10m")
                        .build())
                .retrieve()
                .body(OpenMeteoCurrentWeatherResponseDto.class);

        if (response == null || response.current() == null) {
            throw new IllegalStateException("Unable to retrieve current weather");
        }

        return new WeatherResponseDto(
                city,
                response.current().temperature(),
                response.current().windSpeed(),
                response.current().time());
    }

    @Override
    public Double getAverageSummerTemperature(BigDecimal latitude, BigDecimal longitude) {

        OpenMeteoClientResponseDto response = weatherRestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/climate")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("start_date", "2020-06-01")
                        .queryParam("end_date", "2020-08-31")
                        .queryParam("daily", "temperature_2m_mean")
                        .build())
                .retrieve()
                .body(OpenMeteoClientResponseDto.class);

        if (response == null) {
            throw new IllegalStateException("Unable to retrieve climate data");
        }

        return response.daily().temperatures().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
}

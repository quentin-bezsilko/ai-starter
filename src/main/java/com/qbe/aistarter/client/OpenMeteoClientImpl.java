package com.qbe.aistarter.client;

import com.qbe.aistarter.dto.OpenMeteoClientResponseDto;
import com.qbe.aistarter.dto.OpenMeteoCurrentWeatherResponseDto;
import com.qbe.aistarter.dto.WeatherResponseDto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenMeteoClientImpl implements OpenMeteoClient {

    private static final String FORECAST_PATH = "/v1/forecast";
    private static final String CLIMATE_PATH = "/v1/climate";
    private static final String CURRENT_WEATHER_VARIABLES = "temperature_2m,wind_speed_10m";
    private static final String DAILY_MEAN_TEMPERATURE_VARIABLE = "temperature_2m_mean";
    private static final String SUMMER_START_DATE = "2020-06-01";
    private static final String SUMMER_END_DATE = "2020-08-31";

    private static final BigDecimal MIN_LATITUDE = BigDecimal.valueOf(-90);
    private static final BigDecimal MAX_LATITUDE = BigDecimal.valueOf(90);
    private static final BigDecimal MIN_LONGITUDE = BigDecimal.valueOf(-180);
    private static final BigDecimal MAX_LONGITUDE = BigDecimal.valueOf(180);

    private final RestClient weatherRestClient;

    @Override
    public WeatherResponseDto getCurrentWeather(BigDecimal latitude, BigDecimal longitude, String city) {
        validateCoordinates(latitude, longitude);
        String resolvedCity = normalizeCity(city);

        log.debug(
                "Requesting current weather from Open-Meteo (city='{}', lat={}, lon={})",
                resolvedCity,
                latitude,
                longitude);

        OpenMeteoCurrentWeatherResponseDto response = executeGet(
                () -> weatherRestClient
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path(FORECAST_PATH)
                                .queryParam("latitude", latitude)
                                .queryParam("longitude", longitude)
                                .queryParam("current", CURRENT_WEATHER_VARIABLES)
                                .build())
                        .retrieve()
                        .body(OpenMeteoCurrentWeatherResponseDto.class),
                "Unable to retrieve current weather for city '" + resolvedCity + "'");

        if (response == null || response.current() == null) {
            throw new IllegalStateException(
                    "Unable to retrieve current weather: empty payload for city '" + resolvedCity + "'");
        }

        var current = response.current();
        return new WeatherResponseDto(resolvedCity, current.temperature(), current.windSpeed(), current.time());
    }

    @Override
    public Double getAverageSummerTemperature(BigDecimal latitude, BigDecimal longitude) {
        validateCoordinates(latitude, longitude);

        log.debug(
                "Requesting average summer temperature from Open-Meteo (lat={}, lon={}, period={}..{})",
                latitude,
                longitude,
                SUMMER_START_DATE,
                SUMMER_END_DATE);

        OpenMeteoClientResponseDto response = executeGet(
                () -> weatherRestClient
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path(CLIMATE_PATH)
                                .queryParam("latitude", latitude)
                                .queryParam("longitude", longitude)
                                .queryParam("start_date", SUMMER_START_DATE)
                                .queryParam("end_date", SUMMER_END_DATE)
                                .queryParam("daily", DAILY_MEAN_TEMPERATURE_VARIABLE)
                                .build())
                        .retrieve()
                        .body(OpenMeteoClientResponseDto.class),
                "Unable to retrieve climate data");

        if (response == null || response.daily() == null) {
            throw new IllegalStateException("Unable to retrieve climate data: missing daily block");
        }

        List<Double> temperatures = response.daily().temperatures();
        if (temperatures == null || temperatures.isEmpty()) {
            log.warn(
                    "No daily temperatures returned for lat={}, lon={} over {}..{}",
                    latitude,
                    longitude,
                    SUMMER_START_DATE,
                    SUMMER_END_DATE);
            return 0.0;
        }

        return temperatures.stream()
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private <T> T executeGet(java.util.function.Supplier<T> requestSupplier, String errorMessage) {
        try {
            return requestSupplier.get();
        } catch (RestClientException ex) {
            log.error("{}: {}", errorMessage, ex.getMessage(), ex);
            throw new IllegalStateException(errorMessage, ex);
        }
    }

    private void validateCoordinates(BigDecimal latitude, BigDecimal longitude) {
        Objects.requireNonNull(latitude, "latitude must not be null");
        Objects.requireNonNull(longitude, "longitude must not be null");

        if (latitude.compareTo(MIN_LATITUDE) < 0 || latitude.compareTo(MAX_LATITUDE) > 0) {
            throw new IllegalArgumentException("latitude must be between -90 and 90, got: " + latitude);
        }
        if (longitude.compareTo(MIN_LONGITUDE) < 0 || longitude.compareTo(MAX_LONGITUDE) > 0) {
            throw new IllegalArgumentException("longitude must be between -180 and 180, got: " + longitude);
        }
    }

    private String normalizeCity(String city) {
        Objects.requireNonNull(city, "city must not be null");
        String trimmed = city.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("city must not be blank");
        }
        return trimmed;
    }
}

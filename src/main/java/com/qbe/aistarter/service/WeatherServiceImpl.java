package com.qbe.aistarter.service;

import com.qbe.aistarter.client.OpenMeteoClient;
import com.qbe.aistarter.dto.SummerTemperatureDto;
import com.qbe.aistarter.dto.WeatherResponseDto;
import com.qbe.aistarter.entity.CityEntity;
import com.qbe.aistarter.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final CityRepository cityRepository;

    private final OpenMeteoClient openMeteoClient;

    @Override
    public WeatherResponseDto getCurrentWeather(String city) {
        log.info("Getting current weather for city {}", city);
        CityEntity cityEntity = cityRepository
                .findByName(city)
                .orElseThrow(() -> new IllegalArgumentException("Unknown city: " + city));

        return openMeteoClient.getCurrentWeather(
                cityEntity.getLatitude(), cityEntity.getLongitude(), cityEntity.getName());
    }

    @Override
    public SummerTemperatureDto getAverageSummerTemperature(String city) {
        log.info("Getting average summer temperature for city {}", city);
        CityEntity cityEntity = cityRepository
                .findByName(city)
                .orElseThrow(() -> new IllegalArgumentException("Unknown city: " + city));

        Double averageTemperature =
                openMeteoClient.getAverageSummerTemperature(cityEntity.getLatitude(), cityEntity.getLongitude());

        return new SummerTemperatureDto(cityEntity.getName(), averageTemperature);
    }
}

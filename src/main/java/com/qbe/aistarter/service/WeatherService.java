package com.qbe.aistarter.service;

import com.qbe.aistarter.dto.SummerTemperatureDto;
import com.qbe.aistarter.dto.WeatherResponseDto;

public interface WeatherService {

    WeatherResponseDto getCurrentWeather(String city);

    SummerTemperatureDto getAverageSummerTemperature(String city);
}

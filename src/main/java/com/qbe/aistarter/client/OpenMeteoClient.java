package com.qbe.aistarter.client;

import com.qbe.aistarter.dto.WeatherResponseDto;
import java.math.BigDecimal;

public interface OpenMeteoClient {

    WeatherResponseDto getCurrentWeather(BigDecimal latitude, BigDecimal longitude, String city);

    Double getAverageSummerTemperature(BigDecimal latitude, BigDecimal longitude);
}

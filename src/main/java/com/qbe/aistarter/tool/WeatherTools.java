package com.qbe.aistarter.tool;

import com.qbe.aistarter.dto.SummerTemperatureDto;
import com.qbe.aistarter.dto.WeatherResponseDto;
import com.qbe.aistarter.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeatherTools {

    private final WeatherService weatherService;

    @Tool(
            description =
                    """
                    Returns the current weather conditions for a city.

                    Input: a city name.
                    Output: current temperature, wind speed and observation time.

                    Examples:
                    - Paris
                    - Madrid
                    - Berlin

                    Use this tool whenever the user asks about the current weather,
                    temperature, climate conditions or weather forecast of a city.
                    """)
    public WeatherResponseDto getCurrentWeather(String city) {
        return weatherService.getCurrentWeather(city);
    }

    @Tool(
            description =
                    """
                    Returns the average summer temperature of a city in degrees Celsius.

                    Input: a city name.
                    Output: average temperature during summer.

                    Examples:
                    - Paris
                    - Madrid
                    - Rome

                    Use this tool whenever the user asks about:
                    - average summer temperature
                    - summer climate
                    - how hot a city is during summer
                    - temperature comparison between cities
                    """)
    public SummerTemperatureDto getAverageSummerTemperature(String city) {
        return weatherService.getAverageSummerTemperature(city);
    }
}

package com.qbe.aistarter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class WeatherConfig {

    @Bean
    public RestClient weatherRestClient() {
        return RestClient.builder()
                .baseUrl("https://archive-api.open-meteo.com")
                .build();
    }
}

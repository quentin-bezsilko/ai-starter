package com.qbe.aistarter.config;

import com.qbe.aistarter.tool.GeographyTools;
import com.qbe.aistarter.tool.WeatherTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(GeographyTools geographyTool, WeatherTools weatherTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(geographyTool, weatherTool)
                .build();
    }
}

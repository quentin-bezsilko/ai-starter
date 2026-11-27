package com.qbe.aistarter.loader;

import com.qbe.aistarter.enums.ResponseLevelDto;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromptLoader {

    private static final String PROMPT_LOCATION = "classpath:prompts/";
    private static final String SYSTEM_PROMPT = "expert-assistant.st";

    private final ResourceLoader resourceLoader;
    private final ConcurrentMap<ResponseLevelDto, String> promptCache = new ConcurrentHashMap<>();

    public String loadPromptByResponseLevel(ResponseLevelDto responseLevel) {
        Objects.requireNonNull(responseLevel, "Response level cannot be null");

        return promptCache.computeIfAbsent(responseLevel, level -> {
            String developerPromptFile = level.name().toLowerCase(Locale.ROOT) + "-assistant.st";
            log.debug("Loading prompts for responseLevel={} ({})", level, developerPromptFile);

            String systemPrompt = load(SYSTEM_PROMPT);
            String developerPrompt = load(developerPromptFile);

            return systemPrompt + "\n\n" + developerPrompt;
        });
    }

    private String load(String filename) {
        String location = PROMPT_LOCATION + filename;
        Resource resource = resourceLoader.getResource(location);

        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalStateException("Prompt resource not found or unreadable: " + location);
        }

        try (var inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load prompt: " + filename, exception);
        }
    }
}

package com.qbe.aistarter.loader;

import com.qbe.aistarter.enums.ResponseLevelDto;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
@RequiredArgsConstructor
public class PromptLoaderImpl {

    private static final String PROMPT_LOCATION = "classpath:prompts/";
    private static final String SYSTEM_PROMPT = "expert-system.st";

    private final ResourceLoader resourceLoader;

    public String loadPrompt(ResponseLevelDto responseLevel) {
        if (responseLevel == null) {
            throw new IllegalArgumentException("Response level cannot be null");
        }

        String developerPromptFile = responseLevel.name().toLowerCase(Locale.ROOT) + "-developer.st";

        String systemPrompt = load(SYSTEM_PROMPT);
        String developerPrompt = load(developerPromptFile);

        return systemPrompt + "\n\n" + developerPrompt;
    }

    private String load(String filename) {
        try (var inputStream =
                resourceLoader.getResource(PROMPT_LOCATION + filename).getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load prompt: " + filename, exception);
        }
    }
}

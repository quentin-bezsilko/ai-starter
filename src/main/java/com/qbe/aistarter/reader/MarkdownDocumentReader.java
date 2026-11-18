package com.qbe.aistarter.reader;

import com.qbe.aistarter.entity.DocumentationSourceEntity;
import com.qbe.aistarter.enums.DocumentationSourceType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarkdownDocumentReader implements DocumentationContentReader {

    private final ResourceLoader resourceLoader;

    @Override
    public boolean supports(DocumentationSourceEntity source) {

        if (source.getSourceType() != DocumentationSourceType.FILE || !StringUtils.hasText(source.getUrl())) {
            return false;
        }

        String url = source.getUrl().toLowerCase(Locale.ROOT);

        return url.endsWith(".md") || url.endsWith(".markdown");
    }

    @Override
    public ExtractedDocument read(DocumentationSourceEntity source) {

        if (!supports(source)) {
            throw new IllegalArgumentException("Unsupported Markdown source: " + source.getUrl());
        }

        try {
            Resource resource = resourceLoader.getResource(source.getUrl());

            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalStateException("Markdown file does not exist or is not readable: " + source.getUrl());
            }

            String content;

            try (var inputStream = resource.getInputStream()) {
                content = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            }

            if (!StringUtils.hasText(content)) {
                throw new IllegalStateException("Markdown file is empty: " + source.getUrl());
            }

            String filename = resource.getFilename();

            String title = filename != null ? filename : source.getName();

            log.info("Markdown document successfully read: {}", source.getUrl());

            return new ExtractedDocument(title, source.getUrl(), "text/markdown", content);

        } catch (IOException exception) {

            throw new IllegalStateException("Unable to read Markdown file: " + source.getUrl(), exception);
        }
    }
}

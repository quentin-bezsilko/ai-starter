package com.qbe.aistarter.reader;

import com.qbe.aistarter.entity.DocumentationSourceEntity;
import com.qbe.aistarter.enums.DocumentationSourceType;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class WebDocumentationReader implements DocumentationContentReader {

    private static final int TIMEOUT_MILLIS = 10_000;
    private static final int MAX_RESPONSE_BYTES = 2_000_000;
    private static final int MAX_CRAWL_DEPTH = 3;
    private static final int MAX_PAGES = 50;
    private static final String USER_AGENT = "QBE-AI-Starter-DocumentationBot/1.0";

    @Override
    public boolean supports(DocumentationSourceEntity source) {
        return source.getSourceType() == DocumentationSourceType.WEB && StringUtils.hasText(source.getUrl());
    }

    @Override
    public List<ExtractedDocument> read(DocumentationSourceEntity source) {
        if (!supports(source)) {
            throw new IllegalArgumentException("Unsupported Web source: " + source.getUrl());
        }
        if (!StringUtils.hasText(source.getAllowedHost())) {
            throw new IllegalArgumentException("Allowed host is required for Web source: " + source.getName());
        }

        int maxDepth = crawlDepth(source);
        URI root = validateUrl(source.getUrl(), source.getAllowedHost());
        Queue<CrawlTarget> pending = new ArrayDeque<>();
        Set<URI> visited = new LinkedHashSet<>();
        List<ExtractedDocument> documents = new ArrayList<>();
        pending.add(new CrawlTarget(root, 0));
        visited.add(root);
        int pagesRead = 0;

        while (!pending.isEmpty() && pagesRead < MAX_PAGES) {
            CrawlTarget target = pending.remove();
            pagesRead++;
            Document page = fetch(target.uri());
            String content = page.body().text();

            if (StringUtils.hasText(content)) {
                String title = StringUtils.hasText(page.title())
                        ? page.title()
                        : target.uri().toString();
                documents.add(new ExtractedDocument(title, target.uri().toString(), "text/html", content));
            }

            if (target.depth() < maxDepth) {
                for (var link : page.select("a[href]")) {
                    String href = link.absUrl("href");
                    if (!StringUtils.hasText(href)) {
                        continue;
                    }

                    try {
                        URI candidate = validateUrl(href, source.getAllowedHost());
                        if (visited.add(candidate)) {
                            pending.add(new CrawlTarget(candidate, target.depth() + 1));
                        }
                    } catch (IllegalArgumentException exception) {
                        log.debug("Ignoring out-of-scope documentation link {}", href);
                    }
                }
            }
        }

        if (!pending.isEmpty()) {
            throw new IllegalStateException("Documentation crawl exceeded the page limit of " + MAX_PAGES);
        }

        if (documents.isEmpty()) {
            throw new IllegalStateException("No readable HTML pages found for source: " + source.getUrl());
        }

        log.info("Read {} documentation pages from {}", documents.size(), source.getUrl());
        return List.copyOf(documents);
    }

    URI validateUrl(String rawUrl, String allowedHost) {
        if (!StringUtils.hasText(rawUrl) || !StringUtils.hasText(allowedHost)) {
            throw new IllegalArgumentException("URL and allowed host are required");
        }

        URI uri;
        try {
            uri = new URI(rawUrl.trim()).normalize();
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("Invalid documentation URL: " + rawUrl, exception);
        }

        String scheme = uri.getScheme();
        String host = uri.getHost();
        boolean validScheme = "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
        boolean validPort = uri.getPort() == -1
                || ("http".equalsIgnoreCase(scheme) && uri.getPort() == 80)
                || ("https".equalsIgnoreCase(scheme) && uri.getPort() == 443);

        if (!validScheme
                || host == null
                || uri.getUserInfo() != null
                || !validPort
                || !host.equalsIgnoreCase(allowedHost.trim())
                || isIpLiteral(host)) {
            throw new IllegalArgumentException("URL is outside the configured allowed host: " + rawUrl);
        }

        try {
            return new URI(
                    scheme.toLowerCase(Locale.ROOT),
                    null,
                    host.toLowerCase(Locale.ROOT),
                    uri.getPort(),
                    uri.getPath(),
                    uri.getQuery(),
                    null);
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("Invalid documentation URL: " + rawUrl, exception);
        }
    }

    private Document fetch(URI uri) {
        try {
            Connection.Response response = Jsoup.connect(uri.toString())
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MILLIS)
                    .maxBodySize(MAX_RESPONSE_BYTES)
                    .followRedirects(false)
                    .ignoreHttpErrors(false)
                    .execute();

            if (response.statusCode() != 200) {
                throw new IllegalStateException("Unexpected HTTP status " + response.statusCode() + " for " + uri);
            }
            if (response.contentType() == null
                    || !response.contentType().toLowerCase(Locale.ROOT).contains("text/html")) {
                throw new IllegalStateException("Documentation page is not HTML: " + uri);
            }

            Document document = response.parse();
            document.select("script, style, nav, footer, header, noscript").remove();
            return document;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read documentation page: " + uri, exception);
        }
    }

    private int crawlDepth(DocumentationSourceEntity source) {
        if (!source.isCrawlEnabled()) {
            return 0;
        }
        Integer configuredDepth = source.getMaxDepth();
        if (configuredDepth == null || configuredDepth < 0) {
            throw new IllegalArgumentException("Maximum crawl depth must not be negative");
        }
        return Math.min(configuredDepth, MAX_CRAWL_DEPTH);
    }

    private boolean isIpLiteral(String host) {
        return host.indexOf(':') >= 0 || host.matches("\\d{1,3}(\\.\\d{1,3}){3}");
    }

    private record CrawlTarget(URI uri, int depth) {}
}

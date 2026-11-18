package com.qbe.aistarter.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import org.junit.jupiter.api.Test;

class WebDocumentationReaderTest {

    private final WebDocumentationReader reader = new WebDocumentationReader();

    @Test
    void normalizesAllowedUrlAndRemovesFragment() {
        URI uri = reader.validateUrl("https://DOCS.EXAMPLE.test/a/../reference#section", "docs.example.test");

        assertEquals(URI.create("https://docs.example.test/reference"), uri);
    }

    @Test
    void rejectsHostsOutsideTheExactAllowList() {
        assertThrows(
                IllegalArgumentException.class,
                () -> reader.validateUrl("https://docs.example.test.evil.test/page", "docs.example.test"));
    }

    @Test
    void rejectsNonHttpUrlsAndCredentials() {
        assertThrows(
                IllegalArgumentException.class, () -> reader.validateUrl("file:///etc/passwd", "docs.example.test"));
        assertThrows(
                IllegalArgumentException.class,
                () -> reader.validateUrl("https://user@docs.example.test/page", "docs.example.test"));
    }
}

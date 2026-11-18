package com.qbe.aistarter.reader;

import com.qbe.aistarter.entity.DocumentationSourceEntity;

public interface DocumentationContentReader {

    boolean supports(DocumentationSourceEntity source);

    ExtractedDocument read(DocumentationSourceEntity source);

    record ExtractedDocument(String title, String canonicalUrl, String contentType, String content) {}
}

package com.qbe.aistarter.reader;

import com.qbe.aistarter.entity.DocumentationSourceEntity;
import java.util.List;

public interface DocumentationContentReader {

    boolean supports(DocumentationSourceEntity source);

    List<ExtractedDocument> read(DocumentationSourceEntity source);

    record ExtractedDocument(String title, String canonicalUrl, String contentType, String content) {}
}

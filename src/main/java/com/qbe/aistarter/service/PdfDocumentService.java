package com.qbe.aistarter.service;

import com.qbe.aistarter.dto.IndexedDocumentDto;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface PdfDocumentService {

    void deletePdf(String filename);

    @Transactional
    int ingestOrUpdatePdf(MultipartFile file) throws IOException;

    List<IndexedDocumentDto> listIndexedDocuments();
}

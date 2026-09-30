package com.campus.helpdesk.controller;

import com.campus.helpdesk.model.TicketDocument;
import com.campus.helpdesk.repository.TicketDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class DocumentController {

    @Autowired
    private TicketDocumentRepository documentRepository;

    @GetMapping("/document/download/{docId}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable int docId) {
        Optional<TicketDocument> optDoc = documentRepository.findById(docId);

        if (optDoc.isPresent()) {
            TicketDocument doc = optDoc.get();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                    .contentType(MediaType.parseMediaType(doc.getFileType() != null ? doc.getFileType() : "application/octet-stream"))
                    .body(doc.getFileData());
        }
        return ResponseEntity.notFound().build();
    }
}
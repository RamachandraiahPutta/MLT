package com.example.mlt.controller;

import com.example.mlt.entities.EsDocument;
import com.example.mlt.models.Document;
import com.example.mlt.service.AnalyticsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class MLTController {

    private AnalyticsService mltDocsService;

    public MLTController(AnalyticsService mltDocsService) {
        this.mltDocsService = mltDocsService;
    }

    @PostMapping("/mltDocs")
    public List<Document> findMLTDocs(@RequestBody EsDocument doc) {
        return mltDocsService.getMLTDocs(null, doc.getId(), 0, 20);
    }

    @PostMapping("/classification")
    public Optional<String> getClassification(@RequestBody EsDocument doc) {
        return mltDocsService.getClassification(null, doc.getId());
    }
}

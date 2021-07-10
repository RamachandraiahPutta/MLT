package com.example.mlt.service;

import com.example.mlt.models.Discrepancy;
import com.example.mlt.models.Document;
import com.example.mlt.models.ParentDocument;

import java.util.List;
import java.util.Optional;

public interface AnalyticsService {

    public List<ParentDocument> getDuplicateDocGroups();

    public List<Document> getDuplicateDocs(String docId);

    public List<Document> getMLTDocs(String docId);

    public Optional<String> getClassification(String docId);

    public List<Discrepancy> getDiscrepancies();
}

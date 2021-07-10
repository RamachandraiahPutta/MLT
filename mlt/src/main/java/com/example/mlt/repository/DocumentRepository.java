package com.example.mlt.repository;

import com.example.mlt.models.Document;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface DocumentRepository extends ElasticsearchRepository<Document, String> {

    public List<Document> findDocumentsByDuplicateId(String docId);
}

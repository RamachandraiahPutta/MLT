package com.example.mlt.controller;

import com.example.mlt.entities.EsDocument;
import com.example.mlt.service.MLTDocsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MLTController {

    private MLTDocsService mltDocsService;

    public MLTController(MLTDocsService mltDocsService){
        this.mltDocsService = mltDocsService;
    }

    @PostMapping("/mltDocs")
    public List<EsDocument> findMLTDocs(@RequestBody EsDocument doc){
        return mltDocsService.findMLTDocs(doc.getId());
    }
}

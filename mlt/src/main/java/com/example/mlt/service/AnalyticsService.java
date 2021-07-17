package com.example.mlt.service;

import com.example.mlt.models.AggResult;
import com.example.mlt.models.Discrepancy;
import com.example.mlt.models.Document;
import com.example.mlt.models.ParentDocument;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AnalyticsService {

    /* returns Duplicate document groups per customer
     *  by default it will return only 20 groups
     *  provide last group key to fetch next page
     * */
    public AggResult<List<ParentDocument>> getDuplicateDocGroups(String customerId, Map<String, Object> afterKey);

    /*  returns duplicate documents for a original document.
     *  provide pageNum and pageSize to fetch next page
     * */
    public List<Document> getDuplicateDocs(String customerId, String docId, int pageNum, int pageSize);

    /*  returns more like this documents for a original document.
     *  provide pageNum and pageSize to fetch next page
     * */
    public List<Document> getMLTDocs(String customerId, String docId, int pageNum, int pageSize);

    /*  returns recommended classification string for given document id.
     * */
    public Optional<String> getClassification(String customerId, String docId);

    /*  returns Discrepancy Groups for given customer.
     *  by default it will return only 20 groups
     *  provide last group key to fetch next page
     * */
    public AggResult<List<Discrepancy>> getDiscrepancies(String customerId, Map<String, Object> afterKey);
}

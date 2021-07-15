package com.example.mlt.service;

import com.example.mlt.models.Discrepancy;
import com.example.mlt.models.Document;
import com.example.mlt.models.Pair;
import com.example.mlt.models.ParentDocument;
import com.example.mlt.repository.DocumentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final String DOCS_INDEX = "ediscovery_es";

    @Autowired
    private ObjectMapper objectMapper;

    private DocumentRepository documentRepository;

    private ElasticsearchOperations elasticsearchOperations;

    public AnalyticsServiceImpl(ElasticsearchOperations elasticsearchOperations,
                                DocumentRepository documentRepository) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.documentRepository = documentRepository;
    }

    @Override
    public List<Document> getMLTDocs(String customerId, String docId, int pageNum, int pageSize) {

        QueryBuilder queryBuilder =
                QueryBuilders
                        .moreLikeThisQuery(
                                new String[]{"content"},
                                new String[]{},
                                new MoreLikeThisQueryBuilder.Item[]{
                                        new MoreLikeThisQueryBuilder.Item(DOCS_INDEX, docId)
                                }
                        )
                        .minTermFreq(1)
                        .minDocFreq(2)
                        .minimumShouldMatch("95%")
                        .maxQueryTerms(10000);

        Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .build();

        return getDocumentsForQuery(searchQuery, Document.class);
    }

    @Override
    public Optional<String> getClassification(String customerId, String docId) {

        List<Document> mltDocs = getMLTDocs(null, docId, 0, 20);
        Optional<String> label = mltDocs.stream().collect(
                Collectors.groupingBy(Document::getLabel, Collectors.counting()))
                .entrySet().stream().max(Comparator.comparing(Map.Entry::getValue))
                .map(Map.Entry::getKey);
        return label;
    }

    @Override
    public List<ParentDocument> getDuplicateDocGroups(String customerId, String afterKey) {
        String aggName = "aggByDuplicateId";
        String field = "duplicateId";

        AbstractAggregationBuilder aggBuilder =
                AggregationBuilders
                        .terms(aggName)
                        .field(field)
                        .size(10);

        String subAggName = "topHits";
        AbstractAggregationBuilder subAggBuilder =
                AggregationBuilders
                        .topHits(subAggName)
                        .size(10);

        aggBuilder.subAggregation(subAggBuilder);

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .addAggregation(aggBuilder)
                .build();

        searchQuery.setMaxResults(0);

        Aggregations aggregations = getAggsForQuery(searchQuery, Document.class);

        Terms duplicateDocsBuckets = aggregations.get(aggName);

        List<? extends Terms.Bucket> buckets = duplicateDocsBuckets.getBuckets();

        //bucket key with doc_count
        Map<String, Long> bucketCounts = buckets.stream().collect(Collectors.toMap(Terms.Bucket::getKeyAsString, Terms.Bucket::getDocCount));

        List<ParentDocument> parentDocuments = getDocumentsByIds(bucketCounts.keySet());

        List<ParentDocument> duplicateGroups = new LinkedList<ParentDocument>();
        Map<String, List<Document>> duplicateDocsMap = new HashMap<>();
        for (Terms.Bucket bucket : buckets) {
            TopHits topHits = bucket.getAggregations().get(subAggName);
            org.elasticsearch.search.SearchHit[] hits = topHits.getHits().getHits();
            List<Document> duplicateDocs =
                    Arrays.stream(hits)
                            .map(h -> objectMapper.convertValue(h.getSourceAsString(), Document.class))
                            .collect(Collectors.toList());
            duplicateDocsMap.put(bucket.getKeyAsString(), duplicateDocs);

        }

        parentDocuments.forEach(d -> d.setDuplicates(duplicateDocsMap.get(d.getId())));
        return parentDocuments;
    }

    private List<ParentDocument> getDocumentsByIds(Collection<String> docIds) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withIds(docIds)
                .build();
        return getDocumentsForQuery(searchQuery, ParentDocument.class);

    }

    private <T> List<T> getDocumentsForQuery(Query query, Class<T> cls) {
        SearchHits<T> docHits = getDocHitsForQuery(query, cls);
        return docHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
    }

    private <T> Aggregations getAggsForQuery(Query query, Class<T> cls) {
        SearchHits<T> docHits = getDocHitsForQuery(query, cls);
        return docHits.getAggregations();
    }

    private <T> SearchHits<T> getDocHitsForQuery(Query query, Class<T> cls) {
        SearchHits<T> docHits =
                elasticsearchOperations
                        .search(query,
                                cls,
                                IndexCoordinates.of(DOCS_INDEX));
        return docHits;
    }

    @Override
    public List<Document> getDuplicateDocs(String customerId, String docId, int pageNum, int pageSize) {
        return documentRepository.findDocumentsByDuplicateId(docId);
    }

    @Override
    public List<Discrepancy> getDiscrepancies(String custmerId, String afterKey) {

        String aggName = "groupByLabel";
        String field = "label";

        AbstractAggregationBuilder aggBuilder =
                AggregationBuilders
                        .terms(aggName)
                        .field(field)
                        .size(10);

        String subAggName = "byCompositeField";
        String subAggField = "mc_type";
        AbstractAggregationBuilder subAggBuilder =
                AggregationBuilders
                        .terms(subAggName)
                        .field(subAggField)
                        .size(10);

        aggBuilder.subAggregation(subAggBuilder);

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .addAggregation(aggBuilder)
                .build();

        Aggregations aggregations = getAggsForQuery(searchQuery, Document.class);

        Terms termAggregation = aggregations.get(aggName);

        List<? extends Terms.Bucket> bucketsByLabel = termAggregation.getBuckets();

        Map<String, Long> labelCounts = bucketsByLabel.stream().collect(
                Collectors.toMap(Terms.Bucket::getKeyAsString, Terms.Bucket::getDocCount));

        List<Discrepancy> discrepanciesList = new LinkedList<>();
        for (Terms.Bucket bucket : bucketsByLabel) {
            Terms termSubAggregation = bucket.getAggregations().get(subAggName);
            List<? extends Terms.Bucket> bucketsBymcType = termSubAggregation.getBuckets();
            Pair systemClassification = new Pair(bucket.getKeyAsString(), bucket.getDocCount());
            List<Pair> userClassification = bucketsBymcType.stream()
                    .map(b -> new Pair(b.getKeyAsString(), b.getDocCount()))
                    .collect(Collectors.toList());
            discrepanciesList.add(new Discrepancy(systemClassification, userClassification));
        }
        return discrepanciesList;
    }
}

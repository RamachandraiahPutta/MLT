package com.example.mlt.service;

import com.example.mlt.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregation;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.TermsValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final String DOCS_INDEX = "ediscovery_es";

    @Autowired
    private ObjectMapper objectMapper;

    private ElasticsearchOperations elasticsearchOperations;

    public AnalyticsServiceImpl(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
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
                .build()
                .setPageable(PageRequest.of(pageNum, pageSize));

        return getDocumentsForQuery(searchQuery, Document.class);
    }

    @Override
    public List<Document> getUnlabelledDocuments(String customerId, int pageNum, int pageSize) {
        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("clientid.keyword", customerId))
                .mustNot(QueryBuilders.existsQuery("label"));

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .build()
                .setPageable(PageRequest.of(pageNum, pageSize));

        return getDocumentsForQuery(searchQuery, Document.class);
    }

    @Override
    public AggResult<List<ParentDocument>> getDuplicateDocGroups(String customerId, Map<String, Object> afterKey) {
        String aggName = "aggByduplicateId";
        String field = "duplicateId.keyword";

        List<CompositeValuesSourceBuilder<?>> sourceBuilderList = new ArrayList<>();
        sourceBuilderList.add(new TermsValuesSourceBuilder(aggName).field(field));

        CompositeAggregationBuilder aggBuilder =
                AggregationBuilders
                        .composite(aggName, sourceBuilderList)
                        .size(20);

        if (Objects.nonNull(afterKey))
            aggBuilder.aggregateAfter(afterKey);

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

        CompositeAggregation duplicateDocsBuckets = aggregations.get(aggName);
        afterKey = duplicateDocsBuckets.afterKey();

        List<? extends CompositeAggregation.Bucket> buckets = duplicateDocsBuckets.getBuckets();

        //bucket key with doc_count
        Map<Map<String, Object>, Long> bucketCounts = buckets.stream().collect(Collectors.toMap(CompositeAggregation.Bucket::getKey, CompositeAggregation.Bucket::getDocCount));
        System.out.println(bucketCounts.keySet());
        String[] documentIds = bucketCounts.keySet().stream().map(m -> String.valueOf(m.get(aggName))).toArray(String[]::new);
        System.out.println(documentIds);
        List<ParentDocument> parentDocuments = getDocumentsByIds(documentIds);
        System.out.println(parentDocuments.size());
        Map<String, List<Document>> duplicateDocsMap = new HashMap<>();
        for (CompositeAggregation.Bucket bucket : buckets) {
            TopHits topHits = bucket.getAggregations().get(subAggName);
            org.elasticsearch.search.SearchHit[] hits = topHits.getHits().getHits();
            List<Document> duplicateDocs =
                    Arrays.stream(hits)
                            .map(h -> {
                                try {
                                    return objectMapper.readValue(h.getSourceAsString(), Document.class);
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .collect(Collectors.toList());
            duplicateDocsMap.put(String.valueOf(bucket.getKey().get(aggName)), duplicateDocs);
            System.out.println(bucket.getKeyAsString());
            System.out.println(duplicateDocs);
        }

        parentDocuments.forEach(d -> d.setDuplicates(duplicateDocsMap.get(d.getId())));
        return new AggResult<>(parentDocuments, afterKey);
    }

    private List<ParentDocument> getDocumentsByIds(String[] docIds) {

        QueryBuilder idsQuery = QueryBuilders.idsQuery().addIds(docIds);
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(idsQuery)
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

        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("clientid.keyword", customerId))
                .must(QueryBuilders.termQuery("id", docId));

        QueryBuilder filterQuery = QueryBuilders
                .boolQuery()
                .filter(queryBuilder);

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(filterQuery)
                .build()
                .setPageable(PageRequest.of(pageNum, pageSize));

        return getDocumentsForQuery(searchQuery, Document.class);
    }

    @Override
    public AggResult<List<Discrepancy>> getDiscrepancies(String customerId, Map<String, Object> afterKey) {

        String aggName = "groupByLabel";
        String field = "label.keyword";

        List<CompositeValuesSourceBuilder<?>> sourceBuilderList = new ArrayList<>();
        sourceBuilderList.add(new TermsValuesSourceBuilder(aggName).field(field));

        CompositeAggregationBuilder aggBuilder =
                AggregationBuilders
                        .composite(aggName, sourceBuilderList)
                        .size(20);

        if (Objects.nonNull(afterKey))
            aggBuilder.aggregateAfter(afterKey);

        String subAggName = "byCompositeField";
        String subAggField = "mc_type.keyword";
        AbstractAggregationBuilder subAggBuilder =
                AggregationBuilders
                        .terms(subAggName)
                        .field(subAggField)
                        .size(10);

        aggBuilder.subAggregation(subAggBuilder);

        QueryBuilder queryBuilder = QueryBuilders
                .termQuery("clientid.keyword", customerId);

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .addAggregation(aggBuilder)
                .build();

        searchQuery.setMaxResults(0);

        Aggregations aggregations = getAggsForQuery(searchQuery, Document.class);

        CompositeAggregation compositeAggregation = aggregations.get(aggName);
        afterKey = compositeAggregation.afterKey();

        List<? extends CompositeAggregation.Bucket> bucketsByLabel = compositeAggregation.getBuckets();

        Map<String, Long> labelCounts = bucketsByLabel.stream().collect(
                Collectors.toMap(CompositeAggregation.Bucket::getKeyAsString, CompositeAggregation.Bucket::getDocCount));

        List<Discrepancy> discrepanciesList = new LinkedList<>();
        for (CompositeAggregation.Bucket bucket : bucketsByLabel) {
            Terms termSubAggregation = bucket.getAggregations().get(subAggName);
            List<? extends Terms.Bucket> bucketsBymcType = termSubAggregation.getBuckets();
            Pair systemClassification = new Pair(String.valueOf(bucket.getKey().get(aggName)), bucket.getDocCount());
            List<Pair> userClassification = bucketsBymcType.stream()
                    .map(b -> new Pair(b.getKeyAsString(), b.getDocCount()))
                    .collect(Collectors.toList());
            discrepanciesList.add(new Discrepancy(systemClassification, userClassification));
        }
        return new AggResult<>(discrepanciesList, afterKey);
    }

    @Override
    public boolean tagDuplicate(String customerId, String docId) {
        List<Document> mltDocs = getMLTDocs(customerId, docId, 0, 20);
        if (mltDocs.isEmpty())
            return false;
        Document parentDocument = mltDocs.get(0);
        String duplicateId = getDuplicateId(parentDocument);
        Optional<String> label = getClassification(mltDocs);

        StringBuilder script = new StringBuilder(String.format("ctx._source.duplicatedId = '%s'; ", duplicateId));

        label.ifPresent(l -> script.append(String.format("ctx._source.label = '%s'; ", l)));
        UpdateQuery updateQuery = UpdateQuery.builder(docId).withScript(script.toString()).build();

        UpdateResponse updateResponse = elasticsearchOperations.update(updateQuery, IndexCoordinates.of(DOCS_INDEX));
        log.debug(updateResponse.toString());
        return true;
    }

    private String getDuplicateId(Document mltDoc) {
        if (Objects.nonNull(mltDoc.getDuplicateId()))
            return mltDoc.getDuplicateId();
        return mltDoc.getId();
    }

    private Optional<String> getClassification(List<Document> mltDocs) {
        if (Objects.isNull(mltDocs))
            return Optional.empty();

        Optional<String> label = mltDocs.stream().filter(d -> Objects.nonNull(d.getLabel())).collect(
                Collectors.groupingBy(Document::getLabel, Collectors.counting()))
                .entrySet().stream().max(Comparator.comparing(Map.Entry::getValue))
                .map(Map.Entry::getKey);
        return label;
    }
}

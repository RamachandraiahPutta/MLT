package com.example.mlt.service;

import com.example.mlt.entities.EsDocument;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MLTDocsService {

    private static final String DOCS_INDEX = "ediscovery_es";

    private ElasticsearchOperations elasticsearchOperations;

    public MLTDocsService(ElasticsearchOperations elasticsearchOperations){
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public List<EsDocument> findMLTDocs(final String docId) {

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

        SearchHits<EsDocument> docsHits =
                elasticsearchOperations
                        .search(searchQuery,
                                EsDocument.class,
                                IndexCoordinates.of(DOCS_INDEX));
        List<EsDocument> mltDocs = docsHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
        return mltDocs;
    }

    public Optional<String> getClassification(String docId, String field){

        List<EsDocument> mltDocs = findMLTDocs(docId);
        Optional<String> label = mltDocs.stream().collect(Collectors.groupingBy(EsDocument::getDepartment, Collectors.counting()))
                .entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).map(Map.Entry::getKey);
        return label;
    }
}

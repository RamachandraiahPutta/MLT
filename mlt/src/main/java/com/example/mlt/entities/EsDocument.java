package com.example.mlt.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Document(indexName = "ediscovery_es")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class EsDocument {

    @Id
    private String id;
    private String uri;
    private String jobId;
    private int resolve_type;
    private String clientid;
    private String clientname;
    private String partitioncode;
    private int doc_size;
    private String filesize;
    private int xmptpgnpages;
    private String resolve_status;
    private String pdfencrypted;
    private String mimetype;
    private String allow_token_document;
    private String deny_token_document;
    private String allow_token_share;
    private String deny_token_share;
    private String allow_token_parent;
    private String deny_token_parent;
    private String author;
    private String resourcename;
    private int stream_size;
    private String targetpartition;
    private String targetFolder;
    private String location;
    private String department;
    private String dmsTemplate;
    private String staticPath;
    private String src_directory_structure;
    private String securityId;
    @Field(type = FieldType.Text, name = "contentType")
    private String contentType;
    @Field(type = FieldType.Text, name = "content")
    private String content;
    private String dmsId;
    private String mc_type;
    private String mc_subType;
    private int mc_resolve_type;
    private long mc_resolve_timestamp;
    private int is_dms_document;
    private String dmsLocation;
    private String extension;
    private String tikaProcessed;
    private int migration_status;
    private String sourcePath;
    private String jobType;
    private String modifiedBy;
    private String oneToOneMapping;
}

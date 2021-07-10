package com.example.mlt.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Document {
    public String id;
    public String uri;
    @JsonProperty(value = "clientid")
    public String clientId;
    @JsonProperty(value = "clientname")
    public String clientName;
    @JsonProperty(value = "partitioncode")
    public String partitionCode;
    @JsonProperty(value = "resolve_status")
    public String resolveStatus;
    @JsonProperty(value = "createddate")
    public Date createdDate;
    @JsonProperty(value = "lastmodifieddate")
    public Date lastModifiedDate;
    @JsonProperty(value = "indexdate")
    public Date indexDate;
    @JsonProperty(value = "mimetype")
    public String mimeType;
    public Object author;
    @JsonProperty(value = "resourcename")
    public Object resourceName;
    @JsonProperty(value = "targetPartition")
    public String targetpartition;
    public String targetFolder;
    public String location;
    public String department;
    public String dmsTemplate;
    public String staticPath;
    @JsonProperty(value = "src_directory_structure")
    public String srcDirectoryStructure;
    @JsonProperty(value = "contenttype")
    public Object contentType;
    public Object dmsId;
    @JsonProperty(value = "mc_type")
    public Object mcType;
    @JsonProperty(value = "mc_subType")
    public Object mcSubType;
    @JsonProperty(value = "mc_resolve_type")
    public int mcResolveType;
    public String extension;
    public String sourcePath;
    public Object modifiedBy;
    public String duplicateId;
    public String label;
}

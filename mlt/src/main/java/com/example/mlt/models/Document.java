package com.example.mlt.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class Document {
    @Id
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
    public String author;
    @JsonProperty(value = "resourcename")
    public String resourceName;
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
    public String contentType;
    public String dmsId;
    @JsonProperty(value = "mc_type")
    public String mcType;
    @JsonProperty(value = "mc_subType")
    public String mcSubType;
    @JsonProperty(value = "mc_resolve_type")
    public int mcResolveType;
    public String extension;
    public String sourcePath;
    public String modifiedBy;
    public String duplicateId;
    public String label;
}

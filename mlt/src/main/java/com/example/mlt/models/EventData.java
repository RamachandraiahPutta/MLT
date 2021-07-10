package com.example.mlt.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@NoArgsConstructor
@SuperBuilder
public class EventData {
    private String id;
    @JsonProperty(value = "clientid")
    private String clientId;
    @JsonProperty(value = "clientname")
    private String clientName;
    @JsonProperty(value = "partitioncode")
    private String partitionCode;
    @JsonProperty(value = "mimetype")
    private String mimeType;
    @JsonProperty(value = "mc_type")
    private String mcType;
    @JsonProperty(value = "mc_subType")
    private String mcSubType;
}

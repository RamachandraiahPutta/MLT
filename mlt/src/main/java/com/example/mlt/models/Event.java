package com.example.mlt.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@SuperBuilder
public class Event<T>{
    private String id;
    private String source;
    @JsonProperty(value = "specversion")
    private String specVersion;
    private Date time;
    private String type;
    private String schemaType;
    private String tenantId;
    private T data;

}

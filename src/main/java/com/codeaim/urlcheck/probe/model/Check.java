package com.codeaim.urlcheck.probe.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Set;

@Getter
@Setter
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Check
{
    private long id;
    private long userId;
    private String name;
    private String url;
    private Status status;
    private Long latestResultId;
    private boolean confirming;
    private Set<Header> headers;
}
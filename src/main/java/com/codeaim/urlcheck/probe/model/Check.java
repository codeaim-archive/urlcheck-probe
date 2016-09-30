package com.codeaim.urlcheck.probe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@Accessors(chain = true)
public class Check
{
    private long id;
    private String name;
    private String url;
    private Status status;
    private Long latestResultId;
    private boolean confirming;
}

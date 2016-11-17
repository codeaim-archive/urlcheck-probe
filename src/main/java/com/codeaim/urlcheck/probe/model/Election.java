package com.codeaim.urlcheck.probe.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@Accessors(chain = true)
public class Election
{
    private String name;
    private boolean clustered;
    private long candidateLimit;
    private String username;
}

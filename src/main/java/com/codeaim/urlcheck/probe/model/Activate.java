package com.codeaim.urlcheck.probe.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@Accessors(chain = true)
public class Activate implements Serializable
{
    private Instant created;
}

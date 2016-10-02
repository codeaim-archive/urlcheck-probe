package com.codeaim.urlcheck.probe.message;

import com.codeaim.urlcheck.probe.model.Result;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class Results
{
    private String correlationId;
    private Result[] results;
}

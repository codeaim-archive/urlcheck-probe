package com.codeaim.urlcheck.probe.message;

import com.codeaim.urlcheck.probe.model.Check;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class Checks
{
    private String correlationId;
    private Check[] checks;
}

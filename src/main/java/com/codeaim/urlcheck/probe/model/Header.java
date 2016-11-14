package com.codeaim.urlcheck.probe.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class Header
{
    private String name;
    private String value;
}

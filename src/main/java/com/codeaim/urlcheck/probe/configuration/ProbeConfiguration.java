package com.codeaim.urlcheck.probe.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "urlcheck.probe")
public class ProbeConfiguration
{
    private String name = "Standalone";
    private int candidateLimit = 25;
    private int executorThreadPoolSize = 50;
    private boolean clustered;
    private boolean scheduleDisabled;
    private String getCandidatesEndpoint = "http://localhost:6601/probe/candidate";
    private String createResultsEndpoint = "http://localhost:6601/probe/result";
    private int activateDelay = 2000;
    private int connectTimeout = 2000;
    private int readTimeout = 2000;
    private int writeTimeout = 2000;
}


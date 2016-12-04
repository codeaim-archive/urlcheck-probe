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
    private String name = "probe-local";
    private int candidateLimit = 25;
    private int expireBatchSize = 50000;
    private int executorThreadPoolSize = 50;
    private boolean clustered;
    private boolean scheduleDisabled;
    private String getCandidatesEndpoint = "http://api.urlcheck.io/probe/candidate";
    private String createResultsEndpoint = "http://api.urlcheck.io/probe/result";
    private String expireResultsEndpoint = "http://api.urlcheck.io/probe/expire";
    private int activateElectionDelay = 2000;
    private int activateResultExpiryDelay = 60000;
    private int metricReportDelay = 60000;
    private int connectTimeout = 2000;
    private int readTimeout = 2000;
    private int writeTimeout = 2000;
    private String username;
}


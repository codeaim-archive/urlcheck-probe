package com.codeaim.urlcheck.probe.task;

import com.codeaim.urlcheck.probe.client.ApiClient;
import com.codeaim.urlcheck.probe.configuration.ProbeConfiguration;
import com.codeaim.urlcheck.probe.message.Results;
import com.codeaim.urlcheck.probe.utility.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class UpdateTask
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ProbeConfiguration probeConfiguration;
    private ApiClient apiClient;

    @Autowired
    public UpdateTask(
            ProbeConfiguration probeConfiguration,
            ApiClient apiClient
    )
    {
        this.probeConfiguration = probeConfiguration;
        this.apiClient = apiClient;
    }

    @JmsListener(destination = Queue.CHECK_RESULTS)
    public void receiveMessage(Results results)
    {
        MDC.put("name", probeConfiguration.getName());
        MDC.put("correlationId", results.getCorrelationId());
        logger.debug("UpdateTask received CHECK_RESULTS message with " + results.getResults().length + " results");
        if (results.getResults().length > 0)
            apiClient.createResults(results);
    }
}

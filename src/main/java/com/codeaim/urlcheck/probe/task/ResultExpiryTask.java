package com.codeaim.urlcheck.probe.task;

import com.codeaim.urlcheck.probe.client.ApiClient;
import com.codeaim.urlcheck.probe.configuration.ProbeConfiguration;
import com.codeaim.urlcheck.probe.message.Activate;
import com.codeaim.urlcheck.probe.utility.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class ResultExpiryTask
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ProbeConfiguration probeConfiguration;
    private ApiClient apiClient;

    @Autowired
    public ResultExpiryTask(
            ProbeConfiguration probeConfiguration,
            ApiClient apiClient
    )
    {
        this.probeConfiguration = probeConfiguration;
        this.apiClient = apiClient;
    }

    @JmsListener(destination = Queue.ACTIVATE_RESULT_EXPIRY)
    public void receiveMessage(Activate activate)
    {
        MDC.put("name", probeConfiguration.getName());
        MDC.put("correlationId", activate.getCorrelationId());
        logger.debug("ResultExpiryTask received ACTIVATE_RESULT_EXPIRY message");
        apiClient.expireResults();
    }
}

package com.codeaim.urlcheck.probe.task;

import com.codeaim.urlcheck.probe.configuration.ProbeConfiguration;
import com.codeaim.urlcheck.probe.message.Activate;
import com.codeaim.urlcheck.probe.model.Expire;
import com.codeaim.urlcheck.probe.utility.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ResultExpiryTask
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ProbeConfiguration probeConfiguration;
    private RestTemplate restTemplate;

    @Autowired
    public ResultExpiryTask(
            ProbeConfiguration probeConfiguration,
            RestTemplate restTemplate
    )
    {
        this.probeConfiguration = probeConfiguration;
        this.restTemplate = restTemplate;
    }

    @JmsListener(destination = Queue.ACTIVATE_RESULT_EXPIRY)
    public void receiveMessage(Activate activate)
    {
        MDC.put("name", probeConfiguration.getName());
        MDC.put("correlationId", activate.getCorrelationId());
        logger.debug("ResultExpiryTask received ACTIVATE_RESULT_EXPIRY message");

        expireResults(activate);
    }

    private void expireResults(Activate activate)
    {
        try
        {
            logger.debug("ResultExpiryTask expiring results");
            restTemplate
                    .postForObject(
                            probeConfiguration.getExpireResultsEndpoint(),
                            new HttpEntity<>(new Expire()
                                    .setBatchSize(probeConfiguration.getExpireBatchSize())),
                            Void.class);
        } catch (Exception ex)
        {
            logger.error("ResultExpiryTask exception thrown expiring results", ex);
        }
    }
}

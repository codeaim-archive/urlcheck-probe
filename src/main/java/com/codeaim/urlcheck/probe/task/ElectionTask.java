package com.codeaim.urlcheck.probe.task;

import com.codeaim.urlcheck.probe.client.ApiClient;
import com.codeaim.urlcheck.probe.configuration.ProbeConfiguration;
import com.codeaim.urlcheck.probe.message.Activate;
import com.codeaim.urlcheck.probe.message.Checks;
import com.codeaim.urlcheck.probe.model.Check;
import com.codeaim.urlcheck.probe.utility.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class ElectionTask
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ProbeConfiguration probeConfiguration;
    private ApiClient apiClient;
    private JmsTemplate jmsTemplate;

    @Autowired
    public ElectionTask(
            ProbeConfiguration probeConfiguration,
            ApiClient apiClient,
            JmsTemplate jmsTemplate
    )
    {
        this.probeConfiguration = probeConfiguration;
        this.apiClient = apiClient;
        this.jmsTemplate = jmsTemplate;
    }

    @JmsListener(destination = Queue.ACTIVATE_ELECTION)
    public void receiveMessage(Activate activate)
    {
        MDC.put("name", probeConfiguration.getName());
        MDC.put("correlationId", activate.getCorrelationId());
        logger.trace("ElectionTask received ACTIVATE_ELECTION message");

        Check[] checks = apiClient.getCandidates();

        if (checks.length > 0)
        {
            logger.debug("ElectionTask sending ACQUIRED_CHECKS message with " + checks.length + " checks");
            jmsTemplate.convertAndSend(
                    Queue.ACQUIRED_CHECKS,
                    new Checks()
                            .setCorrelationId(activate.getCorrelationId())
                            .setChecks(checks));
        } else
        {
            logger.trace("ElectionTask did not send ACQUIRED_CHECKS message");
        }
    }
}

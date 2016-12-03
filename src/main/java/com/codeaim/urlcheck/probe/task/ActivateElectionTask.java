package com.codeaim.urlcheck.probe.task;

import com.codeaim.urlcheck.probe.configuration.ProbeConfiguration;
import com.codeaim.urlcheck.probe.message.Activate;
import com.codeaim.urlcheck.probe.utility.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class ActivateElectionTask
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ProbeConfiguration probeConfiguration;
    private JmsTemplate jmsTemplate;

    @Autowired
    public ActivateElectionTask(
            ProbeConfiguration probeConfiguration,
            JmsTemplate jmsTemplate
    )
    {
        this.probeConfiguration = probeConfiguration;
        this.jmsTemplate = jmsTemplate;
    }

    public void run()
    {
        Activate activate = new Activate()
                .setCorrelationId(UUID.randomUUID().toString())
                .setCreated(Instant.now());

        MDC.put("name", probeConfiguration.getName());
        MDC.put("correlationId", activate.getCorrelationId());
        logger.trace("ActivateElectionTask sending ACTIVATE_ELECTION message");

        jmsTemplate.convertAndSend(
                Queue.ACTIVATE_ELECTION,
                activate);
    }
}

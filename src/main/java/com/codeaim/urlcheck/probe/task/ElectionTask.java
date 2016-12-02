package com.codeaim.urlcheck.probe.task;

import com.codeaim.urlcheck.probe.configuration.ProbeConfiguration;
import com.codeaim.urlcheck.probe.message.Activate;
import com.codeaim.urlcheck.probe.message.Checks;
import com.codeaim.urlcheck.probe.model.Check;
import com.codeaim.urlcheck.probe.model.Election;
import com.codeaim.urlcheck.probe.utility.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ElectionTask
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ProbeConfiguration probeConfiguration;
    private RestTemplate restTemplate;
    private JmsTemplate jmsTemplate;

    @Autowired
    public ElectionTask(
            ProbeConfiguration probeConfiguration,
            RestTemplate restTemplate,
            JmsTemplate jmsTemplate
    )
    {
        this.probeConfiguration = probeConfiguration;
        this.restTemplate = restTemplate;
        this.jmsTemplate = jmsTemplate;
    }

    @JmsListener(destination = Queue.ACTIVATE_ELECTION)
    public void receiveMessage(Activate activate)
    {
        logger.trace("ElectionTask received ACTIVATE_ELECTION message", activate.getCorrelationId());

        Check[] checks = getCandidates(activate);

        if (checks.length > 0)
        {
            logger.debug("ElectionTask sending ACQUIRED_CHECKS message with " + checks.length + " checks", activate.getCorrelationId());
            jmsTemplate.convertAndSend(
                    Queue.ACQUIRED_CHECKS,
                    new Checks()
                            .setCorrelationId(activate.getCorrelationId())
                            .setChecks(checks));
        } else
        {
            logger.trace("ElectionTask did not send ACQUIRED_CHECKS message", activate.getCorrelationId());
        }
    }

    private Check[] getCandidates(Activate activate)
    {
        try
        {
            logger.debug("ElectionTask getting candidates", activate.getCorrelationId());
            return restTemplate
                    .postForObject(
                            probeConfiguration.getGetCandidatesEndpoint(),
                            new HttpEntity<>(new Election()
                                    .setName(probeConfiguration.getUsername() == null ? probeConfiguration.getName() : probeConfiguration.getUsername())
                                    .setClustered(probeConfiguration.isClustered() && probeConfiguration.getUsername() == null)
                                    .setCandidateLimit(probeConfiguration.getCandidateLimit())
                                    .setUsername(probeConfiguration.getUsername())),
                            Check[].class);
        } catch (Exception ex)
        {
            logger.error("ElectionTask exception thrown getting candidates", activate.getCorrelationId(), ex);
            return new Check[0];
        }
    }
}

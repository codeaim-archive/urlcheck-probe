package com.codeaim.urlcheck.probe.task;

import com.codeaim.urlcheck.probe.configuration.ProbeConfiguration;
import com.codeaim.urlcheck.probe.message.Activate;
import com.codeaim.urlcheck.probe.message.Checks;
import com.codeaim.urlcheck.probe.model.Check;
import com.codeaim.urlcheck.probe.model.Election;
import com.codeaim.urlcheck.probe.utility.Queue;
import org.jboss.logging.MDC;
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
        MDC.put("correlationId", activate.getCorrelationId());
        logger.trace("ElectionTask received ACTIVATE_ELECTION message");

        Check[] checks = getCandidates(activate);

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

    private Check[] getCandidates(Activate activate)
    {
        try
        {
            logger.trace("ElectionTask getting candidates");
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
            logger.error("ElectionTask exception thrown getting candidates", ex);
            return new Check[0];
        }
    }
}

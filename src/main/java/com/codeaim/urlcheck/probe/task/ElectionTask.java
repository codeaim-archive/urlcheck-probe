package com.codeaim.urlcheck.probe.task;

import com.codeaim.urlcheck.probe.configuration.ProbeConfiguration;
import com.codeaim.urlcheck.probe.message.Activate;
import com.codeaim.urlcheck.probe.message.Checks;
import com.codeaim.urlcheck.probe.model.Check;
import com.codeaim.urlcheck.probe.model.Election;
import com.codeaim.urlcheck.probe.utility.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ElectionTask
{
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
        System.out.println(activate.getCorrelationId() + ": ElectionTask received ACTIVATE_ELECTION message");
        Check[] checks = getCandidates(activate);


        if (checks.length > 0)
        {
            System.out.println(activate.getCorrelationId() + ": ElectionTask sending ACQUIRED_CHECKS message with " + checks.length + " checks");
            jmsTemplate.convertAndSend(
                    Queue.ACQUIRED_CHECKS,
                    new Checks()
                            .setCorrelationId(activate.getCorrelationId())
                            .setChecks(checks));
        } else
        {
            System.out.println(activate.getCorrelationId() + ": ElectionTask did not send ACQUIRED_CHECKS message");
        }
    }

    private Check[] getCandidates(Activate activate)
    {
        try
        {
            System.out.println(activate.getCorrelationId() + ": ElectionTask getting candidates");
            return restTemplate
                    .postForObject(
                            probeConfiguration.getGetCandidatesEndpoint(),
                            new HttpEntity<>(new Election()
                                    .setName(probeConfiguration.getName())
                                    .setClustered(probeConfiguration.isClustered())
                                    .setCandidateLimit(probeConfiguration.getCandidateLimit())),
                            Check[].class);
        } catch (Exception ex)
        {
            System.out.println(activate.getCorrelationId() + ": ElectionTask exception thrown getting candidates");
            return new Check[0];
        }
    }
}

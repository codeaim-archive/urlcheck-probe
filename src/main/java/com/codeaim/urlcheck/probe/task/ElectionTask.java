package com.codeaim.urlcheck.probe.task;

import com.codeaim.urlcheck.probe.configuration.ProbeConfiguration;
import com.codeaim.urlcheck.probe.model.Activate;
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
        Check[] checks = restTemplate
                .postForObject(
                        probeConfiguration.getGetCandidatesEndpoint(),
                        new HttpEntity<>(new Election()
                                .setName(probeConfiguration.getName())
                                .setClustered(probeConfiguration.isClustered())
                                .setCandidateLimit(probeConfiguration.getCandidateLimit())),
                        Check[].class);

        if (checks.length > 0)
        {
            jmsTemplate.convertAndSend(
                    Queue.ACQUIRED_CHECKS,
                    checks);
        }
    }
}

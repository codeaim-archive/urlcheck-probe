package com.codeaim.urlcheck.probe.task;

import com.codeaim.urlcheck.probe.configuration.ProbeConfiguration;
import com.codeaim.urlcheck.probe.message.Activate;
import com.codeaim.urlcheck.probe.model.Expire;
import com.codeaim.urlcheck.probe.utility.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ResultExpiryTask
{
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
        System.out.println(activate.getCorrelationId() + ": ResultExpiryTask received ACTIVATE_RESULT_EXPIRY message");

        expireResults(activate);
    }

    private void expireResults(Activate activate)
    {
        try
        {
            System.out.println(activate.getCorrelationId() + ": ResultExpiryTask expiring results");
            restTemplate
                    .postForObject(
                            probeConfiguration.getExpireResultsEndpoint(),
                            new HttpEntity<>(new Expire()
                                    .setBatchSize(probeConfiguration.getExpireBatchSize())),
                            Void.class);
        } catch (Exception ex)
        {
            System.out.println(activate.getCorrelationId() + ": ResultExpiryTask exception thrown expiring results");
        }
    }
}

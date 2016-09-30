package com.codeaim.urlcheck.probe.task;

import com.codeaim.urlcheck.probe.configuration.ProbeConfiguration;
import com.codeaim.urlcheck.probe.message.Results;
import com.codeaim.urlcheck.probe.utility.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UpdateTask
{
    private ProbeConfiguration probeConfiguration;
    private RestTemplate restTemplate;

    @Autowired
    public UpdateTask(
            ProbeConfiguration probeConfiguration,
            RestTemplate restTemplate
    )
    {
        this.probeConfiguration = probeConfiguration;
        this.restTemplate = restTemplate;
    }

    @JmsListener(destination = Queue.CHECK_RESULTS)
    public void receiveMessage(Results results)
    {
        System.out.println(results.getCorrelationId() + ": UpdateTask received CHECK_RESULTS message with " + results.getResults().length + " results");
        if (results.getResults().length > 0)
            createResults(results);
    }

    private void createResults(Results results)
    {
        try
        {
            System.out.println(results.getCorrelationId() + ": UpdateTask create results");
            restTemplate
                    .postForObject(
                            probeConfiguration.getCreateResultsEndpoint(),
                            new HttpEntity<>(results.getResults()),
                            Void.class);
        } catch (Exception ex)
        {
            System.out.println(": UpdateTask exception thrown creating results");
        }
    }
}

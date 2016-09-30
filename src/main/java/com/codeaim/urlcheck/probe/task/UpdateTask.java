package com.codeaim.urlcheck.probe.task;

import com.codeaim.urlcheck.probe.configuration.ProbeConfiguration;
import com.codeaim.urlcheck.probe.utility.Queue;
import com.codeaim.urlcheck.probe.model.Result;
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
    public void receiveMessage(Result[] results)
    {
        if(results.length > 0)
        {
            restTemplate
                    .postForObject(
                            probeConfiguration.getCreateResultsEndpoint(),
                            new HttpEntity<>(results),
                            Void.class);
        }
    }
}

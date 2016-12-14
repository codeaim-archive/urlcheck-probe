package com.codeaim.urlcheck.probe.client;

import com.codeaim.urlcheck.probe.configuration.ProbeConfiguration;
import com.codeaim.urlcheck.probe.message.Results;
import com.codeaim.urlcheck.probe.model.Check;
import com.codeaim.urlcheck.probe.model.Election;
import com.codeaim.urlcheck.probe.model.Expire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApiClient
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ProbeConfiguration probeConfiguration;
    private RestTemplate restTemplate;

    @Autowired
    public ApiClient(
            ProbeConfiguration probeConfiguration,
            RestTemplate restTemplate
    )
    {
        this.probeConfiguration = probeConfiguration;
        this.restTemplate = restTemplate;
    }

    public Check[] getCandidates()
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

    public void expireResults()
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

    public void createResults(Results results)
    {
        try
        {
            logger.debug("UpdateTask create results");
            restTemplate
                    .postForObject(
                            probeConfiguration.getCreateResultsEndpoint(),
                            new HttpEntity<>(results.getResults()),
                            Void.class);
        } catch (Exception ex)
        {
            logger.error("UpdateTask exception thrown creating results", ex);
        }
    }
}

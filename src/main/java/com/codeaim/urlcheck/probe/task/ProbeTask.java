package com.codeaim.urlcheck.probe.task;

import com.codeaim.urlcheck.probe.configuration.ProbeConfiguration;
import com.codeaim.urlcheck.probe.message.Checks;
import com.codeaim.urlcheck.probe.message.Results;
import com.codeaim.urlcheck.probe.model.Check;
import com.codeaim.urlcheck.probe.model.Result;
import com.codeaim.urlcheck.probe.model.Status;
import com.codeaim.urlcheck.probe.utility.Queue;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.AsyncRestTemplate;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class ProbeTask
{
    private ProbeConfiguration probeConfiguration;
    private AsyncRestTemplate asyncRestTemplate;
    private JmsTemplate jmsTemplate;

    @Autowired
    public ProbeTask(
            ProbeConfiguration probeConfiguration,
            AsyncRestTemplate asyncRestTemplate,
            JmsTemplate jmsTemplate
    )
    {
        this.probeConfiguration = probeConfiguration;
        this.asyncRestTemplate = asyncRestTemplate;
        this.jmsTemplate = jmsTemplate;
    }

    @JmsListener(destination = Queue.ACQUIRED_CHECKS, concurrency = "1")
    public void receiveMessage(Checks checks)
    {
        try
        {
            System.out.println(checks.getCorrelationId() + ": ProbeTask received ACQUIRED_CHECKS message with " + checks.getChecks().length + " checks");
            List<Optional<ResponseEntity<Void>>> responses = getResponses(checks);
            List<Pair<Check, Optional<ResponseEntity<Void>>>> checkResponsePairs = getCheckResponsePairs(checks, responses);
            Result[] results = getResults(checkResponsePairs, checks.getCorrelationId());

            if (results.length > 0)
            {
                System.out.println(checks.getCorrelationId() + ": ProbeTask sending CHECK_RESULTS message with " + results.length + " results");
                jmsTemplate.convertAndSend(
                        Queue.CHECK_RESULTS,
                        new Results()
                                .setCorrelationId(checks.getCorrelationId())
                                .setResults(results));
            } else
            {
                System.out.println(checks.getCorrelationId() + ": ProbeTask did not send CHECK_RESULTS message");
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(checks.getCorrelationId() + ": ProbeTask MAJOR receiveMessage exception XXXXXXXXXXX");
        }
    }

    private Result[] getResults(
            List<Pair<Check, Optional<ResponseEntity<Void>>>> checkResponsePairs,
            long correlationId
    )
    {
        try
        {
            return checkResponsePairs.stream()
                    .map(checkResponsePair -> new Result()
                            .setCheckId(checkResponsePair
                                    .getKey()
                                    .getId())
                            .setPreviousResultId(checkResponsePair
                                    .getKey()
                                    .getLatestResultId())
                            .setStatus(checkResponsePair
                                    .getValue()
                                    .map(ResponseEntity::getStatusCode)
                                    .orElse(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .is2xxSuccessful() ? Status.UP : Status.DOWN)
                            .setProbe(probeConfiguration.getName())
                            .setStatusCode(checkResponsePair
                                    .getValue()
                                    .map(response -> response.getStatusCode().value())
                                    .orElse(HttpStatus.INTERNAL_SERVER_ERROR.value()))
//                        .setResponseTime(checkResponsePair
//                                .getValue()
//                                .map(response -> Integer.parseInt(response
//                                        .getHeaders()
//                                        .get(Header.RESPONSE_TIME)
//                                        .get(0)))
//                                .orElse(null))
                            .setChanged(!Objects
                                    .equals(
                                            checkResponsePair
                                                    .getValue()
                                                    .map(ResponseEntity::getStatusCode)
                                                    .orElse(HttpStatus.INTERNAL_SERVER_ERROR)
                                                    .is2xxSuccessful() ? Status.UP : Status.DOWN,
                                            checkResponsePair
                                                    .getKey()
                                                    .getStatus()))
                            .setConfirmation(checkResponsePair
                                    .getKey()
                                    .isConfirming())
                            .setCreated(Instant.now()))
                    .toArray(Result[]::new);
        } catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(correlationId + ": ProbeTask MAJOR getResults exception XXXXXXXXXXX");
            return new Result[0];
        }
    }

    private List<Pair<Check, Optional<ResponseEntity<Void>>>> getCheckResponsePairs(Checks checks, List<Optional<ResponseEntity<Void>>> responses)
    {
        try
        {
            return IntStream
                    .range(0, checks.getChecks().length)
                    .mapToObj(index -> Pair.of(checks.getChecks()[index], responses.get(index)))
                    .collect(Collectors.toList());
        } catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(checks.getCorrelationId() + ": ProbeTask MAJOR getCheckResponsePairs exception XXXXXXXXXXX");
            return Collections.emptyList();
        }
    }

    private List<Optional<ResponseEntity<Void>>> getResponses(Checks checks)
    {
        try
        {

            return Arrays.stream(checks.getChecks())
                    .map(x ->
                    {
                        System.out.println(checks.getCorrelationId() + ": ProbeTask making a request for " + x.getUrl());
                        return asyncRestTemplate.getForEntity(x.getUrl(), Void.class);
                    })
                    .map(x ->
                    {
                        try
                        {
                            return Optional.of(x.get());
                        } catch (Exception e)
                        {
                            System.out.println(checks.getCorrelationId() + ": ProbeTask exception making a request");
                            return Optional.<ResponseEntity<Void>>empty();
                        }
                    }).collect(Collectors.toList());
        } catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(checks.getCorrelationId() + ": ProbeTask MAJOR getResponses exception XXXXXXXXXXX");
            return Collections.emptyList();
        }
    }
}

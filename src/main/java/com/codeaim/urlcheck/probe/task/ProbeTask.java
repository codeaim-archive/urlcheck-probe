package com.codeaim.urlcheck.probe.task;

import com.codeaim.urlcheck.probe.configuration.ProbeConfiguration;
import com.codeaim.urlcheck.probe.model.Check;
import com.codeaim.urlcheck.probe.model.Result;
import com.codeaim.urlcheck.probe.model.Status;
import com.codeaim.urlcheck.probe.utility.Futures;
import com.codeaim.urlcheck.probe.utility.Header;
import com.codeaim.urlcheck.probe.utility.Queue;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class ProbeTask
{
    private ProbeConfiguration probeConfiguration;
    private RestTemplate restTemplate;
    private JmsTemplate jmsTemplate;

    @Autowired
    public ProbeTask(
            ProbeConfiguration probeConfiguration,
            RestTemplate restTemplate,
            JmsTemplate jmsTemplate
    )
    {
        this.probeConfiguration = probeConfiguration;
        this.restTemplate = restTemplate;
        this.jmsTemplate = jmsTemplate;
    }

    @JmsListener(destination = Queue.ACQUIRED_CHECKS)
    public void receiveMessage(Check[] checks)
    {
        Result[] results = mapResponsesToResults(requestCheckResponses(checks));

        if (results.length > 0)
        {
            jmsTemplate.convertAndSend(
                    Queue.CHECK_RESULTS,
                    results
            );
        }

    }

    private List<Pair<Check, Optional<ResponseEntity<Void>>>> requestCheckResponses(Check[] checks)
    {
        try
        {
            List<Optional<ResponseEntity<Void>>> responses = Futures.complete(Arrays.stream(checks)
                    .map(Check::getUrl)
                    .map(url ->
                            CompletableFuture.supplyAsync(() -> requestCheckResponse(url)))
                    .collect(Collectors.toList()))
                    .get();

            return IntStream
                    .range(0, checks.length)
                    .mapToObj(index -> Pair.of(checks[index], responses.get(index)))
                    .collect(Collectors.toList());
        } catch (Exception e)
        {
            System.out.println("requestCheckResponses error");
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private Optional<ResponseEntity<Void>> requestCheckResponse(String url)
    {
        System.out.println("Making request for " + url);
        try
        {
            return Optional.of(restTemplate.getForEntity(url, Void.class));
        } catch (Exception e)
        {
            System.out.println("requestCheckResponse error");
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private Result[] mapResponsesToResults(List<Pair<Check, Optional<ResponseEntity<Void>>>> checkResponsePairs)
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
    }
}

package com.codeaim.urlcheck.probe.task;

import com.codeaim.urlcheck.probe.configuration.ProbeConfiguration;
import com.codeaim.urlcheck.probe.message.Checks;
import com.codeaim.urlcheck.probe.message.Results;
import com.codeaim.urlcheck.probe.model.Check;
import com.codeaim.urlcheck.probe.model.Header;
import com.codeaim.urlcheck.probe.model.Result;
import com.codeaim.urlcheck.probe.model.Status;
import com.codeaim.urlcheck.probe.utility.Futures;
import com.codeaim.urlcheck.probe.utility.Queue;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class ProbeTask
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ProbeConfiguration probeConfiguration;
    private OkHttpClient httpClient;
    private ExecutorService executorService;
    private JmsTemplate jmsTemplate;

    @Autowired
    public ProbeTask(
            ProbeConfiguration probeConfiguration,
            OkHttpClient httpClient,
            ExecutorService executorService,
            JmsTemplate jmsTemplate
    )
    {
        this.probeConfiguration = probeConfiguration;
        this.httpClient = httpClient;
        this.executorService = executorService;
        this.jmsTemplate = jmsTemplate;
    }

    @JmsListener(destination = Queue.ACQUIRED_CHECKS, concurrency = "5")
    public void receiveMessage(Checks checks)
    {
        MDC.put("correlationId", checks.getCorrelationId());
        logger.debug("ProbeTask received ACQUIRED_CHECKS message with " + checks.getChecks().length + " checks");
        List<Optional<Response>> responses = getResponses(checks);
        List<Pair<Check, Optional<Response>>> checkResponsePairs = getCheckResponsePairs(checks, responses);
        Result[] results = getResults(checkResponsePairs, checks.getCorrelationId());

        if (results.length > 0)
        {
            logger.debug("ProbeTask sending CHECK_RESULTS message with " + results.length + " results");
            jmsTemplate.convertAndSend(
                    Queue.CHECK_RESULTS,
                    new Results()
                            .setCorrelationId(checks.getCorrelationId())
                            .setResults(results)
            );
        } else
        {
            logger.debug("ProbeTask did not send CHECK_RESULTS message");
        }
    }

    private Result[] getResults(
            List<Pair<Check, Optional<Response>>> checkResponsePairs,
            String correlationId
    )
    {
        return checkResponsePairs.stream()
                .map(checkResponsePair ->
                {
                    Result result = new Result()
                            .setCheckId(checkResponsePair
                                    .getKey()
                                    .getId())
                            .setPreviousResultId(checkResponsePair
                                    .getKey()
                                    .getLatestResultId())
                            .setStatus(isSuccessful(checkResponsePair
                                    .getValue()
                                    .map(Response::code)
                                    .orElse(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                                    ? Status.UP : Status.DOWN)
                            .setProbe(probeConfiguration.getUsername() == null ? probeConfiguration.getName() : probeConfiguration.getUsername())
                            .setStatusCode(checkResponsePair
                                    .getValue()
                                    .map(Response::code)
                                    .orElse(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                            .setResponseTime(checkResponsePair
                                    .getValue()
                                    .map(response -> (int) (response.receivedResponseAtMillis() - response.sentRequestAtMillis()))
                                    .orElse(null))
                            .setChanged(!Objects
                                    .equals(
                                            isSuccessful(checkResponsePair
                                                    .getValue()
                                                    .map(Response::code)
                                                    .orElse(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                                                    ? Status.UP : Status.DOWN,
                                            checkResponsePair
                                                    .getKey()
                                                    .getStatus()
                                    ))
                            .setConfirmation(checkResponsePair
                                    .getKey()
                                    .isConfirming())
                            .setCreated(Instant.now());

                    checkResponsePair
                            .getValue()
                            .ifPresent(Response::close);

                    return result;
                })
                .toArray(Result[]::new);
    }

    private boolean isSuccessful(Integer integer)
    {
        return integer < 299 && integer > 199;
    }

    private List<Pair<Check, Optional<Response>>> getCheckResponsePairs(Checks checks, List<Optional<Response>> responses)
    {
        try
        {
            return IntStream
                    .range(0, checks.getChecks().length)
                    .mapToObj(index -> Pair.of(checks.getChecks()[index], responses.get(index)))
                    .collect(Collectors.toList());
        } catch (Exception ex)
        {
            logger.error("ProbeTask getCheckResponsePairs exception", ex);
            return Collections.emptyList();
        }
    }

    private List<Optional<Response>> getResponses(Checks checks)
    {
        try
        {
            return Futures.complete(Arrays
                    .stream(checks.getChecks())
                    .map(check -> new Request.Builder()
                            .url(check.getUrl())
                            .headers(Headers.of(check.getHeaders() != null ? check
                                    .getHeaders()
                                    .stream()
                                    .collect(Collectors.toMap(Header::getName, Header::getValue)) :
                                    Collections.emptyMap()))
                            .build())
                    .map(checkUrlRequest ->
                            CompletableFuture.supplyAsync(() -> requestCheckResponse(checkUrlRequest), executorService))
                    .collect(Collectors.toList()))
                    .get();
        } catch (Exception ex)
        {
            logger.error("ProbeTask getResponses exception", ex);
            return Collections.emptyList();
        }
    }

    private Optional<Response> requestCheckResponse(Request checkUrlRequest)
    {
        logger.debug("ProbeTask making a request for " + checkUrlRequest.url().toString());
        try
        {
            return Optional.of(httpClient.newCall(checkUrlRequest).execute());
        } catch (Exception ex)
        {
            return Optional.empty();
        }
    }
}

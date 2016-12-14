package com.codeaim.urlcheck.probe.client;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class CheckClient
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private OkHttpClient httpClient;

    @Autowired
    public CheckClient(
            OkHttpClient httpClient
    )
    {
        this.httpClient = httpClient;
    }

    public Optional<Response> requestCheckResponse(
            String probeName,
            String correlationId,
            long checkId,
            long userId,
            String name,
            Request checkUrlRequest
    )
    {
        MDC.put("name", probeName);
        MDC.put("correlationId", correlationId);
        logger.debug(
                "ProbeTask making a request for: { \"checkId\":\"{}\", \"userId\":\"{}\",\"name\":\"{}\",\"url\":\"{}\" }",
                checkId,
                userId,
                name,
                checkUrlRequest.url().toString()
        );
        try
        {
            return Optional.of(httpClient.newCall(checkUrlRequest).execute());
        } catch (Exception ex)
        {
            return Optional.empty();
        }
    }
}

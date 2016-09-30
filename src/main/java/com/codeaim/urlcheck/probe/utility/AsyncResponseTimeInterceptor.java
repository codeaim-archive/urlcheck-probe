package com.codeaim.urlcheck.probe.utility;

import com.google.common.base.Stopwatch;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.AsyncClientHttpRequestExecution;
import org.springframework.http.client.AsyncClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AsyncResponseTimeInterceptor implements AsyncClientHttpRequestInterceptor
{
    @Override
    public ListenableFuture<ClientHttpResponse> intercept(HttpRequest request, byte[] bytes, AsyncClientHttpRequestExecution execution) throws IOException
    {
        Stopwatch stopwatch = Stopwatch.createStarted();
        ListenableFuture<ClientHttpResponse> future = execution.executeAsync(request, bytes);
        future.addCallback(
                resp -> {
                    stopwatch.stop();
                    resp.getHeaders().add(Header.RESPONSE_TIME, String.valueOf(stopwatch.elapsed(TimeUnit.MILLISECONDS)));
                },
                ex -> {
                    stopwatch.stop();
                });
        return future;
    }
}

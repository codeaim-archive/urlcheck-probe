package com.codeaim.urlcheck.probe.utility;

import com.google.common.base.Stopwatch;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ResponseTimeInterceptor implements ClientHttpRequestInterceptor
{
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException
    {
        System.out.println("intercept");
        Stopwatch stopwatch = Stopwatch.createStarted();
        ClientHttpResponse httpResponse = execution.execute(request, body);
        stopwatch.stop();

        httpResponse.getHeaders().add(Header.RESPONSE_TIME, String.valueOf(stopwatch.elapsed(TimeUnit.MILLISECONDS)));

        return httpResponse;
    }
}

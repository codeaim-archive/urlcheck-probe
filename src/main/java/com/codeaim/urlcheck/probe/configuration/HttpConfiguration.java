package com.codeaim.urlcheck.probe.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.AsyncRestTemplate;

@Configuration
public class HttpConfiguration
{
    @Bean
    AsyncRestTemplate asyncRestTemplate() {
        AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();
        //asyncRestTemplate.setInterceptors(ImmutableList.of(new AsyncResponseTimeInterceptor()));
        return asyncRestTemplate;
    }
}

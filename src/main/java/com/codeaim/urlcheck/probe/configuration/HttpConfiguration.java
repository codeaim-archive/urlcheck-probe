package com.codeaim.urlcheck.probe.configuration;

import com.codeaim.urlcheck.probe.utility.ResponseTimeInterceptor;
import com.google.common.collect.ImmutableList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpConfiguration
{
    @Bean
    RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(ImmutableList.of(new ResponseTimeInterceptor()));
        return restTemplate;
    }
}

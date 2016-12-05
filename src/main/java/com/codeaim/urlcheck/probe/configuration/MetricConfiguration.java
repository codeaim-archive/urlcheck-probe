package com.codeaim.urlcheck.probe.configuration;

import com.codahale.metrics.MetricRegistry;
import com.codeaim.urlcheck.probe.metric.MetricReporter;
import com.codeaim.urlcheck.probe.metric.ServiceMetrics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.actuate.metrics.dropwizard.DropwizardMetricServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class MetricConfiguration
{
    @Bean
    public MetricRegistry metricRegistry()
    {
        MetricRegistry metricRegistry = new MetricRegistry();
        metricRegistry.registerAll(new ServiceMetrics());
        return metricRegistry;
    }

    @Bean
    public MetricReporter metricReporter(
            ProbeConfiguration probeConfiguration,
            ObjectMapper objectMapper,
            DropwizardMetricServices dropwizardMetricServices
    )
    {
        MetricReporter metrics = new MetricReporter(
                probeConfiguration,
                objectMapper,
                dropwizardMetricServices,
                metricRegistry()
        );
        metrics.start(
                probeConfiguration.getMetricReportDelay(),
                TimeUnit.MILLISECONDS);
        return metrics;
    }
}

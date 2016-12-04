package com.codeaim.urlcheck.probe.metric;

import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
import com.codeaim.urlcheck.probe.configuration.ProbeConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.metrics.dropwizard.DropwizardMetricServices;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetricReporter extends ScheduledReporter
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ProbeConfiguration probeConfiguration;
    private final ObjectMapper objectMapper;
    private final DropwizardMetricServices metricServices;

    public MetricReporter(
            ProbeConfiguration probeConfiguration,
            ObjectMapper objectMapper,
            DropwizardMetricServices metricServices,
            MetricRegistry metricRegistry
    )
    {
        super(
                metricRegistry,
                probeConfiguration.getName(),
                MetricFilter.ALL,
                TimeUnit.SECONDS,
                TimeUnit.SECONDS
        );

        this.probeConfiguration = probeConfiguration;
        this.objectMapper = objectMapper;
        this.metricServices = metricServices;
    }

    @Override
    public void report(
            SortedMap<String, Gauge> gauges,
            SortedMap<String, Counter> counters,
            SortedMap<String, Histogram> histograms,
            SortedMap<String, Meter> meters,
            SortedMap<String, Timer> timers
    )
    {
        MDC.put("name", probeConfiguration.getName());
        MDC.put("correlationId", UUID.randomUUID().toString());

        try
        {
            logger.info("Metrics report: " + objectMapper.writeValueAsString(
                    Stream.of(
                            mapGauges(gauges),
                            mapCounters(counters),
                            mapHistograms(histograms),
                            mapMeters(meters),
                            mapTimers(timers))
                            .flatMap(x -> x)
                            .collect(Collectors.toMap(
                                    AbstractMap.SimpleEntry::getKey,
                                    AbstractMap.SimpleEntry::getValue
                            ))));

            counters.keySet().forEach(metricServices::reset);

        } catch (JsonProcessingException ex)
        {
            logger.error("MetricReporter exception thrown processing metrcs", ex);
        }
    }

    private Stream<AbstractMap.SimpleEntry<String, Object>> mapTimers(SortedMap<String, Timer> timers)
    {
        return timers
                .entrySet()
                .stream()
                .map(x -> new AbstractMap.SimpleEntry<>(
                        x.getKey(),
                        String.valueOf(x.getValue().getCount())
                ));
    }

    private Stream<AbstractMap.SimpleEntry<String, Object>> mapMeters(SortedMap<String, Meter> meters)
    {
        return meters
                .entrySet()
                .stream()
                .map(x -> new AbstractMap.SimpleEntry<>(
                        x.getKey(),
                        x.getValue().getCount()
                ));
    }

    private Stream<AbstractMap.SimpleEntry<String, Object>> mapHistograms(SortedMap<String, Histogram> histograms)
    {
        return histograms
                .entrySet()
                .stream()
                .map(x -> new AbstractMap.SimpleEntry<>(
                        x.getKey(),
                        x.getValue().getCount()
                ));
    }

    private Stream<AbstractMap.SimpleEntry<String, Object>> mapCounters(SortedMap<String, Counter> counters)
    {
        return counters
                .entrySet()
                .stream()
                .map(x -> new AbstractMap.SimpleEntry<>(
                        x.getKey(),
                        x.getValue().getCount()
                ));
    }

    private Stream<AbstractMap.SimpleEntry<String, Object>> mapGauges(SortedMap<String, Gauge> gauges)
    {
        return gauges
                .entrySet()
                .stream()
                .map(x -> new AbstractMap.SimpleEntry<>(
                        x.getKey(),
                        x.getValue().getValue()
                ));
    }
}

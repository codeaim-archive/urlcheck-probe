package com.codeaim.urlcheck.probe.configuration;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

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
}


class ServiceMetrics implements MetricSet
{
    private long timestamp;
    private Runtime runtime;
    private MemoryMXBean memoryDetails;
    private RuntimeMXBean runtimeDetails;
    private OperatingSystemMXBean osDetails;

    ServiceMetrics()
    {
        this.timestamp = System.currentTimeMillis();
        this.runtime = Runtime.getRuntime();
        this.memoryDetails = ManagementFactory.getMemoryMXBean();
        this.runtimeDetails = ManagementFactory.getRuntimeMXBean();
        this.osDetails = ManagementFactory.getOperatingSystemMXBean();
    }

    @Override
    public Map<String, Metric> getMetrics()
    {
        Map<String, Metric> metrics = new HashMap<>();
        metrics.put("total.memory", (Gauge) () -> runtime.totalMemory());
        metrics.put("free", (Gauge) () -> runtime.freeMemory() + memoryDetails.getNonHeapMemoryUsage().getUsed());
        metrics.put("processors", (Gauge) () -> runtime.availableProcessors());
        metrics.put("instance.uptime", (Gauge) () -> System.currentTimeMillis() - this.timestamp);
        metrics.put("uptime", (Gauge) () -> runtimeDetails.getUptime());
        metrics.put("systemload.average", (Gauge) () -> osDetails.getSystemLoadAverage());
        metrics.put("load", (Gauge) () -> osDetails.getSystemLoadAverage() / runtime.availableProcessors());
        return metrics;
    }
}

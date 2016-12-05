package com.codeaim.urlcheck.probe.configuration;

import com.codeaim.urlcheck.probe.task.ActivateElectionTask;
import com.codeaim.urlcheck.probe.task.ActivateResultExpiryTask;
import com.codeaim.urlcheck.probe.task.MetricReportTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "urlcheck.probe.scheduleDisabled", havingValue = "false", matchIfMissing = true)
public class ScheduleConfiguration
{
    private ProbeConfiguration probeConfiguration;
    private ActivateElectionTask activateElectionTask;
    private ActivateResultExpiryTask activateResultExpiryTask;
    private MetricReportTask metricReportTask;

    @Autowired
    public ScheduleConfiguration(
            ProbeConfiguration probeConfiguration,
            ActivateElectionTask activateElectionTask,
            ActivateResultExpiryTask activateResultExpiryTask,
            MetricReportTask metricReportTask
    )
    {
        this.probeConfiguration = probeConfiguration;
        this.activateElectionTask = activateElectionTask;
        this.activateResultExpiryTask = activateResultExpiryTask;
        this.metricReportTask = metricReportTask;
    }

    @Scheduled(fixedDelayString = "${urlcheck.probe.activateElectionDelay}")
    public void activateElectionTask()
    {
        if(!probeConfiguration.isActivateElectionTaskDisabled())
            activateElectionTask.run();
    }

    @Scheduled(fixedDelayString = "${urlcheck.probe.activateResultExpiryDelay}")
    public void activateResultExpiryTask()
    {
        if(!probeConfiguration.isActivateResultExpiryTaskDisabled())
            activateResultExpiryTask.run();
    }

    @Scheduled(fixedDelayString = "${urlcheck.probe.metricReportDelay}")
    public void metricReportTask()
    {
        if (!probeConfiguration.isMetricReportTaskDisabled())
            metricReportTask.run();
    }
}

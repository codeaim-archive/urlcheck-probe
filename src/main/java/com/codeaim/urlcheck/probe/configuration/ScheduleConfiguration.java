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
    private ActivateElectionTask activateElectionTask;
    private ActivateResultExpiryTask activateResultExpiryTask;
    private MetricReportTask metricReportTask;

    @Autowired
    public ScheduleConfiguration(
            ActivateElectionTask activateElectionTask,
            ActivateResultExpiryTask activateResultExpiryTask,
            MetricReportTask metricReportTask
    )
    {

        this.activateElectionTask = activateElectionTask;
        this.activateResultExpiryTask = activateResultExpiryTask;
        this.metricReportTask = metricReportTask;
    }

    @Scheduled(fixedDelayString = "${urlcheck.probe.activateElectionDelay}")
    public void activateElectionTask()
    {
        activateElectionTask.run();
    }

    @Scheduled(fixedDelayString = "${urlcheck.probe.activateResultExpiryDelay}")
    public void activateResultExpiryTask()
    {
        activateResultExpiryTask.run();
    }

    @Scheduled(fixedDelayString = "${urlcheck.probe.metricReportDelay}")
    public void metricReportTask()
    {
        metricReportTask.run();
    }
}

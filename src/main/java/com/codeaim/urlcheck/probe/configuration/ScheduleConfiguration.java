package com.codeaim.urlcheck.probe.configuration;

import com.codeaim.urlcheck.probe.task.ActivateElectionTask;
import com.codeaim.urlcheck.probe.task.ActivateResultExpiryTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class ScheduleConfiguration implements SchedulingConfigurer
{
    private ProbeConfiguration probeConfiguration;
    private ActivateElectionTask activateElectionTask;
    private ActivateResultExpiryTask activateResultExpiryTask;

    @Autowired
    public ScheduleConfiguration(
            ProbeConfiguration probeConfiguration,
            ActivateElectionTask activateElectionTask,
            ActivateResultExpiryTask activateResultExpiryTask
    )
    {
        this.probeConfiguration = probeConfiguration;
        this.activateElectionTask = activateElectionTask;
        this.activateResultExpiryTask = activateResultExpiryTask;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar)
    {
        if (!probeConfiguration.isScheduleDisabled())
        {
            taskRegistrar.addFixedDelayTask(() -> this.activateElectionTask.run(), probeConfiguration.getActivateElectionDelay());
            taskRegistrar.addFixedDelayTask(() -> this.activateResultExpiryTask.run(), probeConfiguration.getActivateResultExpiryDelay());
        }
    }
}

package com.codeaim.urlcheck.probe.configuration;

import com.codeaim.urlcheck.probe.task.ActivateTask;
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
    private ActivateTask activateTask;

    @Autowired
    public ScheduleConfiguration(
            ProbeConfiguration probeConfiguration,
            ActivateTask activateTask
    )
    {
        this.probeConfiguration = probeConfiguration;
        this.activateTask = activateTask;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar)
    {
        if (!probeConfiguration.isScheduleDisabled())
        {
            taskRegistrar.addFixedDelayTask(() -> this.activateTask.run(), 2000);
        }
    }
}

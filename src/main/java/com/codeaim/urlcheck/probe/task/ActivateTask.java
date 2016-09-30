package com.codeaim.urlcheck.probe.task;

import com.codeaim.urlcheck.probe.model.Activate;
import com.codeaim.urlcheck.probe.utility.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ActivateTask
{
    private JmsTemplate jmsTemplate;

    @Autowired
    public ActivateTask(JmsTemplate jmsTemplate)
    {
        this.jmsTemplate = jmsTemplate;
    }

    public void run()
    {
        jmsTemplate.convertAndSend(
                Queue.ACTIVATE_ELECTION,
                new Activate().setCreated(Instant.now()));
    }
}

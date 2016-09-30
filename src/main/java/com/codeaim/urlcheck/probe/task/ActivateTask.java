package com.codeaim.urlcheck.probe.task;

import com.codeaim.urlcheck.probe.message.Activate;
import com.codeaim.urlcheck.probe.utility.Queue;
import org.apache.activemq.util.LongSequenceGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ActivateTask
{
    private JmsTemplate jmsTemplate;
    private LongSequenceGenerator longSequenceGenerator;

    @Autowired
    public ActivateTask(
            JmsTemplate jmsTemplate,
            LongSequenceGenerator longSequenceGenerator
    )
    {
        this.jmsTemplate = jmsTemplate;
        this.longSequenceGenerator = longSequenceGenerator;
    }

    public void run()
    {
        Activate activate = new Activate()
                .setCorrelationId(longSequenceGenerator.getNextSequenceId())
                .setCreated(Instant.now());

        System.out.println(activate.getCorrelationId() + ": ActivateTask sending ACTIVATE_ELECTION message");

        jmsTemplate.convertAndSend(
                Queue.ACTIVATE_ELECTION,
                activate);
    }
}

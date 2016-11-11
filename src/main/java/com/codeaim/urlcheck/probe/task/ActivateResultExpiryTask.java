package com.codeaim.urlcheck.probe.task;

import com.codeaim.urlcheck.probe.message.Activate;
import com.codeaim.urlcheck.probe.utility.Queue;
import org.apache.activemq.util.LongSequenceGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class ActivateResultExpiryTask
{
    private JmsTemplate jmsTemplate;
    private LongSequenceGenerator longSequenceGenerator;

    @Autowired
    public ActivateResultExpiryTask(
            JmsTemplate jmsTemplate
    )
    {
        this.jmsTemplate = jmsTemplate;
    }

    public void run()
    {
        Activate activate = new Activate()
                .setCorrelationId(UUID.randomUUID().toString())
                .setCreated(Instant.now());

        System.out.println(activate.getCorrelationId() + ": ActivateResultExpiryTask sending ACTIVATE_RESULT_EXPIRY message");

        jmsTemplate.convertAndSend(
                Queue.ACTIVATE_RESULT_EXPIRY,
                activate);
    }
}

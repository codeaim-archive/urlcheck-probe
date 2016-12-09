package com.codeaim.urlcheck.probe.aspect;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codeaim.urlcheck.probe.task.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.codahale.metrics.MetricRegistry.name;

@Aspect
@Component
public class CounterAspect
{
    private final Counter activateElections;
    private final Counter activateResultExpiries;
    private final Counter getCandidates;
    private final Counter metricReports;
    private final Counter acquiredChecks;
    private final Counter requestCheckResponses;
    private final Counter resultExpiry;
    private final Counter update;

    @Autowired
    public CounterAspect(
           MetricRegistry metricRegistry
    )
    {
        this.activateElections = metricRegistry.counter(name(ActivateElectionTask.class, "activate-election"));
        this.activateResultExpiries = metricRegistry.counter(name(ActivateResultExpiryTask.class, "activate-result-expiry"));
        this.getCandidates = metricRegistry.counter(name(ElectionTask.class, "get-candidates"));
        this.metricReports = metricRegistry.counter(name(MetricReportTask.class, "metric-report"));
        this.acquiredChecks = metricRegistry.counter(name(ProbeTask.class, "acquired-checks"));
        this.requestCheckResponses = metricRegistry.counter(name(ProbeTask.class, "request-check-response"));
        this.resultExpiry = metricRegistry.counter(name(ResultExpiryTask.class, "result-expiry"));
        this.update = metricRegistry.counter(name(UpdateTask.class, "update"));
    }

    @Around("execution(* com.codeaim.urlcheck.probe.task.ActivateElectionTask.run(..))")
    public Object aroundActivateElectionTaskRun(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        activateElections.inc();
        return proceedingJoinPoint.proceed();
    }

    @Around("execution(* com.codeaim.urlcheck.probe.task.ActivateResultExpiryTask.run(..))")
    public Object aroundActivateResultExpiryTaskRun(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        activateResultExpiries.inc();
        return proceedingJoinPoint.proceed();
    }

    @Around("execution(* com.codeaim.urlcheck.probe.task.ElectionTask.getCandidates(..))")
    public Object aroundElectionTaskGetCandidates(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        getCandidates.inc();
        return proceedingJoinPoint.proceed();
    }

    @Around("execution(* com.codeaim.urlcheck.probe.task.MetricReportTask.run(..))")
    public Object aroundMetricReportTaskRun(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        metricReports.inc();
        return proceedingJoinPoint.proceed();
    }

    @Around("execution(* com.codeaim.urlcheck.probe.task.ProbeTask.receiveMessage(..))")
    public Object aroundProbeTaskAcquiredChecks(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        acquiredChecks.inc();
        return proceedingJoinPoint.proceed();
    }

    @Around("execution(* com.codeaim.urlcheck.probe.task.ProbeTask.requestCheckResponse(..))")
    public Object aroundProbeTaskRequestCheckResponse(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        requestCheckResponses.inc();
        return proceedingJoinPoint.proceed();
    }

    @Around("execution(* com.codeaim.urlcheck.probe.task.ResultExpiryTask.receiveMessage(..))")
    public Object aroundResultExpiryTaskResultExpiry(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        resultExpiry.inc();
        return proceedingJoinPoint.proceed();
    }

    @Around("execution(* com.codeaim.urlcheck.probe.task.UpdateTask.receiveMessage(..))")
    public Object aroundUpdateTaskUpdate(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        update.inc();
        return proceedingJoinPoint.proceed();
    }
}

package com.codeaim.urlcheck.probe.aspect;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        this.activateElections = metricRegistry.counter("activate-election");
        this.activateResultExpiries = metricRegistry.counter("activate-result-expiry");
        this.getCandidates = metricRegistry.counter("get-candidates");
        this.metricReports = metricRegistry.counter("metric-report");
        this.acquiredChecks = metricRegistry.counter("acquired-checks");
        this.requestCheckResponses = metricRegistry.counter("request-check-response");
        this.resultExpiry = metricRegistry.counter("result-expiry");
        this.update = metricRegistry.counter("update");
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

    @Around("execution(* com.codeaim.urlcheck.probe.client.ApiClient.getCandidates(..))")
    public Object aroundApiClientGetCandidates(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
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

    @Around("execution(* com.codeaim.urlcheck.probe.client.CheckClient.requestCheckResponse(..))")
    public Object aroundCheckClientRequestCheckResponse(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
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

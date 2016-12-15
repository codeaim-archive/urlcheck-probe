package com.codeaim.urlcheck.probe.aspect;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CounterAspect
{
    private final Timer activateElection;
    private final Counter activateElectionCount;
    private final Timer activateResultExpiry;
    private final Counter activateResultExpiryCount;
    private final Timer getCandidates;
    private final Counter getCandidatesCount;
    private final Timer metricReports;
    private final Counter metricReportsCount;
    private final Timer acquiredChecks;
    private final Counter acquiredChecksCount;
    private final Timer requestCheckResponses;
    private final Counter requestCheckResponsesCount;
    private final Timer resultExpiry;
    private final Counter resultExpiryCount;
    private final Timer update;
    private final Counter updateCount;

    @Autowired
    public CounterAspect(
            MetricRegistry metricRegistry
    )
    {
        this.activateElection = metricRegistry.timer("activate-election");
        this.activateElectionCount = metricRegistry.counter("activate-election-count");
        this.activateResultExpiry = metricRegistry.timer("activate-result-expiry");
        this.activateResultExpiryCount = metricRegistry.counter("activate-result-expiry-count");
        this.getCandidates = metricRegistry.timer("get-candidates");
        this.getCandidatesCount = metricRegistry.counter("get-candidates-count");
        this.metricReports = metricRegistry.timer("metric-report");
        this.metricReportsCount = metricRegistry.counter("metric-report-count");
        this.acquiredChecks = metricRegistry.timer("acquired-checks");
        this.acquiredChecksCount = metricRegistry.counter("acquired-checks-count");
        this.requestCheckResponses = metricRegistry.timer("request-check-response");
        this.requestCheckResponsesCount = metricRegistry.counter("request-check-response-count");
        this.resultExpiry = metricRegistry.timer("result-expiry");
        this.resultExpiryCount = metricRegistry.counter("result-expiry-count");
        this.update = metricRegistry.timer("update");
        this.updateCount = metricRegistry.counter("update-count");
    }

    @Around("execution(* com.codeaim.urlcheck.probe.task.ActivateElectionTask.run(..))")
    public Object aroundActivateElectionTaskRun(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        activateElectionCount.inc();
        return time(activateElection, proceedingJoinPoint);
    }

    @Around("execution(* com.codeaim.urlcheck.probe.task.ActivateResultExpiryTask.run(..))")
    public Object aroundActivateResultExpiryTaskRun(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        activateResultExpiryCount.inc();
        return time(activateResultExpiry, proceedingJoinPoint);
    }

    @Around("execution(* com.codeaim.urlcheck.probe.client.ApiClient.getCandidates(..))")
    public Object aroundApiClientGetCandidates(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        getCandidatesCount.inc();
        return time(getCandidates, proceedingJoinPoint);
    }

    @Around("execution(* com.codeaim.urlcheck.probe.task.MetricReportTask.run(..))")
    public Object aroundMetricReportTaskRun(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        metricReportsCount.inc();
        return time(metricReports, proceedingJoinPoint);
    }

    @Around("execution(* com.codeaim.urlcheck.probe.task.ProbeTask.receiveMessage(..))")
    public Object aroundProbeTaskAcquiredChecks(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        acquiredChecksCount.inc();
        return time(acquiredChecks, proceedingJoinPoint);
    }

    @Around("execution(* com.codeaim.urlcheck.probe.client.CheckClient.requestCheckResponse(..))")
    public Object aroundCheckClientRequestCheckResponse(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        requestCheckResponsesCount.inc();
        return time(requestCheckResponses, proceedingJoinPoint);
    }

    @Around("execution(* com.codeaim.urlcheck.probe.task.ResultExpiryTask.receiveMessage(..))")
    public Object aroundResultExpiryTaskResultExpiry(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        resultExpiryCount.inc();
        return time(resultExpiry, proceedingJoinPoint);
    }

    @Around("execution(* com.codeaim.urlcheck.probe.task.UpdateTask.receiveMessage(..))")
    public Object aroundUpdateTaskUpdate(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        updateCount.inc();
        return time(update, proceedingJoinPoint);
    }

    private Object time(Timer timer, ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        Timer.Context time = timer.time();

        try
        {
            return proceedingJoinPoint.proceed();
        } finally
        {
            time.stop();
        }
    }
}

package com.hacker.code.application.service;

import com.hacker.code.adapter.job.MarketDataSyncJob;
import com.hacker.code.adapter.job.MomentumTrendJob;
import com.hacker.code.adapter.job.NavSyncJob;
import com.hacker.code.adapter.job.StrategyJob;
import com.hacker.code.application.dto.RunJobResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchJobManualService {

    private final NavSyncJob navSyncJob;
    private final MarketDataSyncJob marketDataSyncJob;
    private final MomentumTrendJob momentumTrendJob;
    private final StrategyJob strategyJob;

    @Qualifier("batchJobExecutor")
    private final Executor batchJobExecutor;

    private final Map<String, Boolean> running = new ConcurrentHashMap<>();

    private static final String JOB_NAV = "nav";
    private static final String JOB_MARKET = "market";
    private static final String JOB_MOMENTUM = "momentum";
    private static final String JOB_STRATEGY = "strategy";
    private static final String JOB_ALL = "all";

    private static final List<String> ALL_JOB_SEQUENCE = List.of(JOB_NAV, JOB_MARKET, JOB_MOMENTUM, JOB_STRATEGY);

    public RunJobResultDTO run(String jobId) {
        String id = jobId == null ? "" : jobId.toLowerCase();
        if (!ALL_JOB_SEQUENCE.contains(id) && !JOB_ALL.equals(id)) {
            RunJobResultDTO dto = new RunJobResultDTO();
            dto.setSuccess(false);
            dto.setMessage("未知任务: " + jobId);
            dto.setJobId(id);
            return dto;
        }

        if (running.putIfAbsent(id, Boolean.TRUE) != null) {
            RunJobResultDTO dto = new RunJobResultDTO();
            dto.setSuccess(false);
            dto.setMessage("任务正在运行中，请勿重复触发: " + jobId);
            dto.setJobId(id);
            return dto;
        }

        CompletableFuture.runAsync(() -> doRun(id), batchJobExecutor);

        RunJobResultDTO dto = new RunJobResultDTO();
        dto.setSuccess(true);
        dto.setMessage("已触发任务: " + jobId + "，正在后台执行，请稍后刷新看板");
        dto.setJobId(id);
        return dto;
    }

    private void doRun(String jobId) {
        try {
            if (JOB_ALL.equals(jobId)) {
                log.info("一键运行全部跑批任务开始");
                for (String id : ALL_JOB_SEQUENCE) {
                    executeSingle(id);
                }
                log.info("一键运行全部跑批任务完成");
            } else {
                executeSingle(jobId);
            }
        } catch (Exception e) {
            log.error("跑批任务执行失败: {}", jobId, e);
        } finally {
            running.remove(jobId);
        }
    }

    private void executeSingle(String jobId) {
        log.info("手动触发跑批任务: {}", jobId);
        switch (jobId) {
            case JOB_NAV -> navSyncJob.dailySync();
            case JOB_MARKET -> marketDataSyncJob.dailySync();
            case JOB_MOMENTUM -> momentumTrendJob.afternoonCompute();
            case JOB_STRATEGY -> strategyJob.weeklyExecute();
            default -> throw new IllegalArgumentException("未知任务: " + jobId);
        }
        log.info("跑批任务执行完成: {}", jobId);
    }
}

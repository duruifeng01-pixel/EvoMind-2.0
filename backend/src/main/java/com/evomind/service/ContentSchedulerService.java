package com.evomind.service;

import com.evomind.entity.ContentCrawlJob;

import java.util.List;

/**
 * 内容采集调度服务接口
 * 负责定时调度内容采集任务
 */
public interface ContentSchedulerService {

    /**
     * 启动定时调度器
     */
    void startScheduler();

    /**
     * 停止定时调度器
     */
    void stopScheduler();

    /**
     * 立即执行一次全量采集
     */
    List<ContentCrawlJob> triggerImmediateCrawl();

    /**
     * 为指定用户触发采集
     */
    List<ContentCrawlJob> triggerCrawlForUser(Long userId);

    /**
     * 为指定信息源触发采集
     */
    ContentCrawlJob triggerCrawlForSource(Long sourceId);

    /**
     * 获取调度器状态
     */
    SchedulerStatus getSchedulerStatus();

    /**
     * 更新采集频率配置
     */
    void updateScheduleConfig(ScheduleConfig config);

    /**
     * 获取待执行的采集任务队列
     */
    List<ContentCrawlJob> getPendingJobs();

    /**
     * 获取最近执行的任务
     */
    List<ContentCrawlJob> getRecentJobs(int limit);

    /**
     * 调度器状态
     */
    class SchedulerStatus {
        private boolean isRunning;
        private long totalJobsExecuted;
        private long totalJobsFailed;
        private String lastExecutionTime;
        private String nextScheduledTime;

        // Getters and Setters
        public boolean isRunning() { return isRunning; }
        public void setRunning(boolean running) { isRunning = running; }
        public long getTotalJobsExecuted() { return totalJobsExecuted; }
        public void setTotalJobsExecuted(long totalJobsExecuted) { this.totalJobsExecuted = totalJobsExecuted; }
        public long getTotalJobsFailed() { return totalJobsFailed; }
        public void setTotalJobsFailed(long totalJobsFailed) { this.totalJobsFailed = totalJobsFailed; }
        public String getLastExecutionTime() { return lastExecutionTime; }
        public void setLastExecutionTime(String lastExecutionTime) { this.lastExecutionTime = lastExecutionTime; }
        public String getNextScheduledTime() { return nextScheduledTime; }
        public void setNextScheduledTime(String nextScheduledTime) { this.nextScheduledTime = nextScheduledTime; }
    }

    /**
     * 调度配置
     */
    class ScheduleConfig {
        private int intervalMinutes;      // 采集间隔（分钟）
        private int batchSize;            // 每批处理的信息源数量
        private int retryAttempts;        // 失败重试次数
        private boolean enabled;          // 是否启用

        // Getters and Setters
        public int getIntervalMinutes() { return intervalMinutes; }
        public void setIntervalMinutes(int intervalMinutes) { this.intervalMinutes = intervalMinutes; }
        public int getBatchSize() { return batchSize; }
        public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
        public int getRetryAttempts() { return retryAttempts; }
        public void setRetryAttempts(int retryAttempts) { this.retryAttempts = retryAttempts; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}

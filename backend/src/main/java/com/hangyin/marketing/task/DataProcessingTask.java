package com.hangyin.marketing.task;

import com.hangyin.marketing.service.DataProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 数据加工定时任务
 * 实现T+1数据更新，自动筛选高潜线索
 */
@Slf4j
@Component
public class DataProcessingTask {
    
    @Autowired
    private DataProcessingService dataProcessingService;
    
    /**
     * 每天凌晨1点执行数据加工任务（T+1更新）
     * cron表达式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDataDaily() {
        log.info("========== 开始执行每日数据加工任务 ==========");
        try {
            int count = dataProcessingService.processDataAndFilterHighPotentialLeads();
            log.info("========== 每日数据加工任务完成，筛选出 {} 条高潜线索 ==========", count);
        } catch (Exception e) {
            log.error("执行每日数据加工任务失败", e);
        }
    }
    
    /**
     * 每小时执行一次数据加工（可选，用于实时性要求高的场景）
     */
    // @Scheduled(cron = "0 0 * * * ?")
    public void processDataHourly() {
        log.info("执行每小时数据加工任务");
        try {
            dataProcessingService.processDataAndFilterHighPotentialLeads();
        } catch (Exception e) {
            log.error("执行每小时数据加工任务失败", e);
        }
    }
}


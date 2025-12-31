package com.hangyin.marketing.service;

import java.util.List;

/**
 * 数据加工及清洗服务接口
 * 负责根据监控指标筛选高潜线索
 */
public interface DataProcessingService {
    
    /**
     * 执行数据加工和清洗
     * 根据监控指标配置，从线索中筛选出高潜线索
     * 
     * @return 筛选出的高潜线索数量
     */
    int processDataAndFilterHighPotentialLeads();
    
    /**
     * 根据监控指标筛选单个线索是否为高潜线索
     * 
     * @param leadId 线索ID
     * @return 是否为高潜线索
     */
    boolean isHighPotentialLead(Long leadId);
    
    /**
     * 批量处理线索，筛选高潜线索
     * 
     * @param leadIds 线索ID列表
     * @return 筛选出的高潜线索数量
     */
    int batchProcessLeads(List<Long> leadIds);
}


package com.hangyin.marketing.service.impl;

import com.hangyin.marketing.common.PageRequest;
import com.hangyin.marketing.common.PageResult;
import com.hangyin.marketing.entity.HighPotentialLead;
import com.hangyin.marketing.mapper.HighPotentialLeadMapper;
import com.hangyin.marketing.service.HighPotentialLeadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 高潜线索服务实现类
 */
@Slf4j
@Service
public class HighPotentialLeadServiceImpl implements HighPotentialLeadService {
    
    @Autowired
    private HighPotentialLeadMapper highPotentialLeadMapper;
    
    @Override
    public PageResult<HighPotentialLead> getHighPotentialLeadList(PageRequest pageRequest, String keyword, String status) {
        Integer offset = pageRequest.getOffset();
        Integer size = pageRequest.getSize();
        
        List<HighPotentialLead> records = highPotentialLeadMapper.selectHighPotentialLeadList(keyword, status, offset, size);
        Long total = highPotentialLeadMapper.countHighPotentialLeads(keyword, status);
        
        return new PageResult<>(total, records, pageRequest.getCurrent(), pageRequest.getSize());
    }
    
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 命中用户总数
        Long totalMatched = highPotentialLeadMapper.countHighPotentialLeads(null, null);
        stats.put("totalMatched", totalMatched != null ? totalMatched : 0L);
        
        // 今日新增线索（按创建日期统计）
        Long todayNew = highPotentialLeadMapper.countTodayNewLeads();
        stats.put("todayNew", todayNew != null ? todayNew : 0L);
        
        // 待营销处理数量
        Long pendingCount = highPotentialLeadMapper.countHighPotentialLeads(null, "待营销处理");
        stats.put("pendingCount", pendingCount != null ? pendingCount : 0L);
        
        // 计算待营销处理占总量百分比
        if (totalMatched != null && totalMatched > 0) {
            double pendingPercentage = (pendingCount != null ? pendingCount.doubleValue() : 0.0) / totalMatched.doubleValue() * 100;
            stats.put("pendingPercentage", Math.round(pendingPercentage * 10.0) / 10.0); // 保留一位小数
        } else {
            stats.put("pendingPercentage", 0.0);
        }
        
        // 计算较昨日的变化百分比
        Long yesterdayTotal = highPotentialLeadMapper.countYesterdayTotalLeads();
        if (yesterdayTotal != null && yesterdayTotal > 0 && totalMatched != null) {
            double changePercentage = ((totalMatched.doubleValue() - yesterdayTotal.doubleValue()) / yesterdayTotal.doubleValue()) * 100;
            stats.put("yesterdayChangePercentage", Math.round(changePercentage * 10.0) / 10.0); // 保留一位小数
        } else if (yesterdayTotal != null && yesterdayTotal == 0 && totalMatched != null && totalMatched > 0) {
            // 如果昨日为0，今日有数据，显示100%增长
            stats.put("yesterdayChangePercentage", 100.0);
        } else {
            stats.put("yesterdayChangePercentage", 0.0);
        }
        
        // 营销转化成功率
        // 计算逻辑：已转化的线索数 / 已处理过的线索数（不包括"待营销处理"状态）
        // 已处理过的线索包括：营销进行中、营销已转化、已联系失效
        Long convertedCount = highPotentialLeadMapper.countHighPotentialLeads(null, "营销已转化");
        Long processingCount = highPotentialLeadMapper.countHighPotentialLeads(null, "营销进行中");
        Long failedCount = highPotentialLeadMapper.countHighPotentialLeads(null, "已联系失效");
        
        // 已处理过的线索总数 = 营销进行中 + 营销已转化 + 已联系失效
        long processedTotal = (processingCount != null ? processingCount : 0L) 
                            + (convertedCount != null ? convertedCount : 0L) 
                            + (failedCount != null ? failedCount : 0L);
        
        if (processedTotal > 0 && convertedCount != null && convertedCount > 0) {
            double conversionRate = (convertedCount.doubleValue() / processedTotal) * 100;
            stats.put("conversionRate", Math.round(conversionRate * 10.0) / 10.0); // 保留一位小数
        } else {
            // 如果没有已处理的线索，转化率为0
            stats.put("conversionRate", 0.0);
        }
        
        return stats;
    }
    
    @Override
    @Transactional
    public void batchCall(List<Long> ids) {
        // TODO: 对接甲方内部外呼系统
        log.info("批量外呼，线索IDs: {}", ids);
        int updatedCount = 0;
        int skippedCount = 0;
        
        // 只更新"待营销处理"状态的线索为"营销进行中"
        for (Long id : ids) {
            HighPotentialLead existingLead = highPotentialLeadMapper.selectById(id);
            if (existingLead != null && "待营销处理".equals(existingLead.getStatus())) {
                HighPotentialLead lead = new HighPotentialLead();
                lead.setId(id);
                lead.setStatus("营销进行中");
                lead.setUpdateTime(LocalDateTime.now());
                highPotentialLeadMapper.updateById(lead);
                updatedCount++;
            } else {
                skippedCount++;
                log.debug("跳过线索ID: {}, 当前状态: {}", id, existingLead != null ? existingLead.getStatus() : "不存在");
            }
        }
        
        log.info("批量外呼完成，更新: {} 条，跳过: {} 条", updatedCount, skippedCount);
    }
    
    @Override
    @Transactional
    public void batchSendSms(List<Long> ids) {
        // TODO: 对接甲方内部短信系统
        log.info("批量发送短信，线索IDs: {}", ids);
        int updatedCount = 0;
        int skippedCount = 0;
        
        // 只更新"待营销处理"状态的线索为"营销进行中"
        for (Long id : ids) {
            HighPotentialLead existingLead = highPotentialLeadMapper.selectById(id);
            if (existingLead != null && "待营销处理".equals(existingLead.getStatus())) {
                HighPotentialLead lead = new HighPotentialLead();
                lead.setId(id);
                lead.setStatus("营销进行中");
                lead.setUpdateTime(LocalDateTime.now());
                highPotentialLeadMapper.updateById(lead);
                updatedCount++;
            } else {
                skippedCount++;
                log.debug("跳过线索ID: {}, 当前状态: {}", id, existingLead != null ? existingLead.getStatus() : "不存在");
            }
        }
        
        log.info("批量发送短信完成，更新: {} 条，跳过: {} 条", updatedCount, skippedCount);
    }
    
    @Override
    @Transactional
    public void updateLeadStatus(Long id, String status) {
        HighPotentialLead lead = new HighPotentialLead();
        lead.setId(id);
        lead.setStatus(status);
        lead.setUpdateTime(LocalDateTime.now());
        highPotentialLeadMapper.updateById(lead);
    }
    
    @Override
    @Transactional
    public void deleteHighPotentialLead(Long id) {
        highPotentialLeadMapper.deleteById(id);
    }
    
    @Override
    @Transactional
    public void batchDeleteHighPotentialLeads(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            highPotentialLeadMapper.deleteBatchIds(ids);
        }
    }
}


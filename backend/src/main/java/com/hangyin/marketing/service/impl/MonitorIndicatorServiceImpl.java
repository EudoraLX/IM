package com.hangyin.marketing.service.impl;

import com.hangyin.marketing.entity.Lead;
import com.hangyin.marketing.entity.MonitorIndicator;
import com.hangyin.marketing.mapper.LeadMapper;
import com.hangyin.marketing.mapper.MonitorIndicatorMapper;
import com.hangyin.marketing.service.MonitorIndicatorService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 监控指标服务实现类
 */
@Slf4j
@Service
public class MonitorIndicatorServiceImpl implements MonitorIndicatorService {
    
    @Autowired
    private MonitorIndicatorMapper monitorIndicatorMapper;
    
    @Autowired
    private LeadMapper leadMapper;
    
    @Override
    public MonitorIndicator getActiveConfig() {
        QueryWrapper<MonitorIndicator> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.orderByDesc("update_time");
        wrapper.last("LIMIT 1");
        return monitorIndicatorMapper.selectOne(wrapper);
    }
    
    @Override
    @Transactional
    public void saveOrUpdateConfig(MonitorIndicator indicator) {
        if (indicator.getId() == null) {
            indicator.setCreateTime(LocalDateTime.now());
            indicator.setUpdateTime(LocalDateTime.now());
            indicator.setStatus(1);
            monitorIndicatorMapper.insert(indicator);
        } else {
            indicator.setUpdateTime(LocalDateTime.now());
            monitorIndicatorMapper.updateById(indicator);
        }
    }
    
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        // 获取当前配置
        MonitorIndicator config = getActiveConfig();
        if (config == null) {
            result.put("dates", new ArrayList<>());
            result.put("leadCounts", new ArrayList<>());
            result.put("totalLeads", 0);
            result.put("estimatedHighPotential", 0);
            return result;
        }
        
        // 获取最近7天的日期
        List<String> dates = new ArrayList<>();
        List<Integer> leadCounts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        // 解析活跃机构
        Set<String> activeOrganizations = new HashSet<>();
        if (config.getActiveOrganizations() != null && !config.getActiveOrganizations().isEmpty()) {
            try {
                List<String> orgList = JSON.parseArray(config.getActiveOrganizations(), String.class);
                if (orgList != null) {
                    activeOrganizations.addAll(orgList);
                }
            } catch (Exception e) {
                log.warn("解析活跃机构失败", e);
            }
        }
        
        // 统计最近7天每天的新增线索数（包括今天）
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = now.minusDays(i);
            String dateStr = date.format(formatter);
            dates.add(dateStr);
            
            // 查询该日期创建的线索数
            QueryWrapper<Lead> wrapper = new QueryWrapper<>();
            wrapper.eq("deleted", 0);
            
            // 计算该日期的开始和结束时间
            LocalDateTime dayStart = date.toLocalDate().atStartOfDay();
            LocalDateTime dayEnd;
            
            // 如果是今天，结束时间使用当前时间；否则使用下一天的开始时间
            if (i == 0) {
                // 今天：从今天00:00:00到当前时间
                dayEnd = now;
            } else {
                // 历史日期：从当天00:00:00到次日00:00:00
                dayEnd = date.toLocalDate().plusDays(1).atStartOfDay();
            }
            
            wrapper.ge("create_time", dayStart);
            wrapper.le("create_time", dayEnd);
            
            // 如果配置了活跃机构，只统计匹配机构的线索
            if (!activeOrganizations.isEmpty()) {
                wrapper.in("source_channel", activeOrganizations);
            }
            
            Long count = leadMapper.selectCount(wrapper);
            leadCounts.add(count.intValue());
        }
        
        // 统计总线索数
        QueryWrapper<Lead> totalWrapper = new QueryWrapper<>();
        totalWrapper.eq("deleted", 0);
        if (!activeOrganizations.isEmpty()) {
            totalWrapper.in("source_channel", activeOrganizations);
        }
        Long totalLeads = leadMapper.selectCount(totalWrapper);
        
        // 估算高潜线索数（基于活跃阈值）
        // 简化计算：假设活跃次数超过阈值的线索占比为30%
        int estimatedHighPotential = (int) (totalLeads * 0.3);
        
        result.put("dates", dates);
        result.put("leadCounts", leadCounts);
        result.put("totalLeads", totalLeads);
        result.put("estimatedHighPotential", estimatedHighPotential);
        result.put("timeRangeDays", config.getTimeRangeDays());
        result.put("activityThreshold", config.getActivityThreshold());
        
        return result;
    }
}


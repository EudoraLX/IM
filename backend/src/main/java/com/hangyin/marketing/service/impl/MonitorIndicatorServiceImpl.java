package com.hangyin.marketing.service.impl;

import com.hangyin.marketing.entity.HighPotentialLead;
import com.hangyin.marketing.entity.Lead;
import com.hangyin.marketing.entity.MonitorIndicator;
import com.hangyin.marketing.mapper.HighPotentialLeadMapper;
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
    
    @Autowired(required = false)
    private HighPotentialLeadMapper highPotentialLeadMapper;
    
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
        
        // 统计最近7天每天的新增线索数和新增高潜线索数（包括今天）
        // 注意：图表显示的是每天新增的线索数，不受活跃机构过滤影响
        List<Integer> highPotentialCounts = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = now.minusDays(i);
            String dateStr = date.format(formatter);
            dates.add(dateStr);
            
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
            
            // 查询该日期创建的新增线索数（统计所有线索，不受活跃机构过滤）
            QueryWrapper<Lead> wrapper = new QueryWrapper<>();
            wrapper.eq("deleted", 0);
            wrapper.ge("create_time", dayStart);
            wrapper.lt("create_time", dayEnd);
            Long count = leadMapper.selectCount(wrapper);
            leadCounts.add(count.intValue());
            
            // 查询该日期新增的高潜线索数
            int highPotentialCount = 0;
            if (highPotentialLeadMapper != null) {
                QueryWrapper<HighPotentialLead> hplWrapper = new QueryWrapper<>();
                hplWrapper.ge("create_time", dayStart);
                hplWrapper.lt("create_time", dayEnd);
                Long hplCount = highPotentialLeadMapper.selectCount(hplWrapper);
                highPotentialCount = hplCount != null ? hplCount.intValue() : 0;
            }
            highPotentialCounts.add(highPotentialCount);
        }
        
        // 统计总线索数（所有未删除的线索，不受活跃机构过滤）
        QueryWrapper<Lead> totalWrapper = new QueryWrapper<>();
        totalWrapper.eq("deleted", 0);
        // 不按活跃机构过滤，统计所有线索
        Long totalLeads = leadMapper.selectCount(totalWrapper);
        
        // 获取实际的高潜线索数（从高潜线索表统计）
        Long estimatedHighPotential = 0L;
        if (highPotentialLeadMapper != null) {
            QueryWrapper<HighPotentialLead> hplWrapper = new QueryWrapper<>();
            estimatedHighPotential = highPotentialLeadMapper.selectCount(hplWrapper);
        } else {
            // 如果没有高潜线索Mapper，使用估算（基于活跃阈值）
            // 简化计算：假设活跃次数超过阈值的线索占比为30%
            estimatedHighPotential = (long) (totalLeads * 0.3);
        }
        
        result.put("dates", dates);
        result.put("leadCounts", leadCounts);
        result.put("highPotentialCounts", highPotentialCounts); // 每天新增的高潜线索数
        result.put("totalLeads", totalLeads);
        result.put("estimatedHighPotential", estimatedHighPotential); // 总高潜线索数
        result.put("timeRangeDays", config.getTimeRangeDays());
        result.put("activityThreshold", config.getActivityThreshold());
        
        return result;
    }
}


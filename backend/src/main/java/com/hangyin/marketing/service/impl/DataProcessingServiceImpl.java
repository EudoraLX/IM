package com.hangyin.marketing.service.impl;

import com.hangyin.marketing.entity.HighPotentialLead;
import com.hangyin.marketing.entity.Lead;
import com.hangyin.marketing.entity.MonitorIndicator;
import com.hangyin.marketing.mapper.HighPotentialLeadMapper;
import com.hangyin.marketing.mapper.LeadMapper;
import com.hangyin.marketing.service.ActivityDataService;
import com.hangyin.marketing.service.DataProcessingService;
import com.hangyin.marketing.service.MonitorIndicatorService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * 数据加工及清洗服务实现类
 */
@Slf4j
@Service
public class DataProcessingServiceImpl implements DataProcessingService {
    
    @Autowired
    private LeadMapper leadMapper;
    
    @Autowired
    private HighPotentialLeadMapper highPotentialLeadMapper;
    
    @Autowired
    private MonitorIndicatorService monitorIndicatorService;
    
    @Autowired(required = false)
    private ActivityDataService activityDataService;
    
    @Override
    @Transactional
    public int processDataAndFilterHighPotentialLeads() {
        log.info("开始执行数据加工和清洗任务...");
        
        // 1. 获取当前启用的监控指标配置
        MonitorIndicator config = monitorIndicatorService.getActiveConfig();
        if (config == null) {
            log.warn("未找到启用的监控指标配置，跳过数据加工");
            return 0;
        }
        
        // 2. 获取所有未删除的线索
        QueryWrapper<Lead> leadWrapper = new QueryWrapper<>();
        leadWrapper.eq("deleted", 0);
        List<Lead> allLeads = leadMapper.selectList(leadWrapper);
        
        if (allLeads == null || allLeads.isEmpty()) {
            log.info("没有可处理的线索");
            return 0;
        }
        
        // 3. 解析监控配置
        Integer timeRangeDays = config.getTimeRangeDays() != null ? config.getTimeRangeDays() : 7;
        Integer activityThreshold = config.getActivityThreshold() != null ? config.getActivityThreshold() : 3;
        Set<String> activeOrganizations = parseOrganizations(config.getActiveOrganizations());
        
        log.info("监控配置 - 时间范围: {}天, 活跃阈值: {}次, 活跃机构: {}", 
                timeRangeDays, activityThreshold, activeOrganizations);
        
        // 4. 筛选高潜线索
        int highPotentialCount = 0;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.minusDays(timeRangeDays);
        
        // 获取已存在的高潜线索ID，避免重复
        QueryWrapper<HighPotentialLead> existingWrapper = new QueryWrapper<>();
        existingWrapper.eq("status", "待营销处理");
        List<HighPotentialLead> existingLeads = highPotentialLeadMapper.selectList(existingWrapper);
        Set<Long> existingLeadIds = new HashSet<>();
        for (HighPotentialLead hpl : existingLeads) {
            existingLeadIds.add(hpl.getLeadId());
        }
        
        for (Lead lead : allLeads) {
            // 跳过已存在的高潜线索
            if (existingLeadIds.contains(lead.getId())) {
                continue;
            }
            
            // 判断是否为高潜线索
            if (isHighPotentialLead(lead, config, startTime, now)) {
                // 创建高潜线索记录
                HighPotentialLead highPotentialLead = createHighPotentialLead(lead, config);
                highPotentialLeadMapper.insert(highPotentialLead);
                
                // 更新线索表，标记已转换为高潜线索
                lead.setIsHighPotential(1);
                lead.setUpdateTime(LocalDateTime.now());
                leadMapper.updateById(lead);
                
                highPotentialCount++;
                log.debug("筛选出高潜线索: leadId={}, customerName={}", lead.getId(), lead.getCustomerName());
            }
        }
        
        log.info("数据加工完成，共筛选出 {} 条高潜线索", highPotentialCount);
        return highPotentialCount;
    }
    
    @Override
    public boolean isHighPotentialLead(Long leadId) {
        Lead lead = leadMapper.selectById(leadId);
        if (lead == null || lead.getDeleted() == 1) {
            return false;
        }
        
        MonitorIndicator config = monitorIndicatorService.getActiveConfig();
        if (config == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.minusDays(config.getTimeRangeDays());
        
        return isHighPotentialLead(lead, config, startTime, now);
    }
    
    @Override
    @Transactional
    public int batchProcessLeads(List<Long> leadIds) {
        if (leadIds == null || leadIds.isEmpty()) {
            return 0;
        }
        
        MonitorIndicator config = monitorIndicatorService.getActiveConfig();
        if (config == null) {
            log.warn("未找到启用的监控指标配置");
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.minusDays(config.getTimeRangeDays());
        
        int count = 0;
        for (Long leadId : leadIds) {
            Lead lead = leadMapper.selectById(leadId);
            if (lead != null && lead.getDeleted() == 0) {
                if (isHighPotentialLead(lead, config, startTime, now)) {
                    // 检查是否已存在
                    QueryWrapper<HighPotentialLead> wrapper = new QueryWrapper<>();
                    wrapper.eq("lead_id", leadId);
                    wrapper.eq("status", "待营销处理");
                    if (highPotentialLeadMapper.selectCount(wrapper) == 0) {
                        HighPotentialLead highPotentialLead = createHighPotentialLead(lead, config);
                        highPotentialLeadMapper.insert(highPotentialLead);
                        
                        // 更新线索表，标记已转换为高潜线索
                        lead.setIsHighPotential(1);
                        lead.setUpdateTime(LocalDateTime.now());
                        leadMapper.updateById(lead);
                        
                        count++;
                    }
                }
            }
        }
        
        return count;
    }
    
    /**
     * 判断线索是否为高潜线索
     */
    private boolean isHighPotentialLead(Lead lead, MonitorIndicator config, 
                                       LocalDateTime startTime, LocalDateTime endTime) {
        // 解析监控配置
        Set<String> activeOrganizations = parseOrganizations(config.getActiveOrganizations());
        Integer activityThreshold = config.getActivityThreshold() != null ? config.getActivityThreshold() : 3;
        List<String> orgList = new ArrayList<>(activeOrganizations);
        
        // 规则1：获取活跃次数（核心判断）
        int activityCount = 0;
        if (activityDataService != null) {
            // 调用活跃度数据服务获取实际活跃次数
            activityCount = activityDataService.getActivityCount(lead.getId(), startTime, endTime, orgList);
            log.debug("线索 {} 在时间范围内活跃次数: {}", lead.getId(), activityCount);
        } else {
            // 如果没有配置活跃度数据服务，使用简化判断
            log.debug("活跃度数据服务未配置，使用简化判断规则");
        }
        
        // 判断1：活跃次数是否超过阈值（最核心的判断）
        if (activityCount >= activityThreshold) {
            log.debug("线索 {} 活跃次数 {} >= 阈值 {}，判定为高潜线索", lead.getId(), activityCount, activityThreshold);
            return true;
        }
        
        // 判断2：活跃机构匹配
        if (!activeOrganizations.isEmpty()) {
            String sourceChannel = lead.getSourceChannel();
            if (sourceChannel != null && activeOrganizations.contains(sourceChannel)) {
                log.debug("线索 {} 来源渠道 {} 匹配活跃机构，判定为高潜线索", lead.getId(), sourceChannel);
                return true;
            }
        }
        
        // 判断3：新线索且创建时间在监控范围内
        if ("新线索".equals(lead.getStatus()) && 
            lead.getCreateTime() != null && 
            lead.getCreateTime().isAfter(startTime)) {
            log.debug("线索 {} 是新线索且在监控范围内，判定为高潜线索", lead.getId());
            return true;
        }
        
        return false;
    }
    
    /**
     * 创建高潜线索记录
     */
    private HighPotentialLead createHighPotentialLead(Lead lead, MonitorIndicator config) {
        HighPotentialLead highPotentialLead = new HighPotentialLead();
        highPotentialLead.setLeadId(lead.getId());
        highPotentialLead.setUserId("USER_" + lead.getId());
        
        // 根据匹配原因设置命中规则
        String matchingRule = determineMatchingRule(lead, config);
        highPotentialLead.setMatchingRule(matchingRule);
        
        highPotentialLead.setLeadSourceSystem("系统自动筛选");
        highPotentialLead.setMatchingDate(LocalDateTime.now());
        highPotentialLead.setStatus("待营销处理");
        highPotentialLead.setHighPotentialReason("符合监控指标配置条件：" + matchingRule);
        
        // 根据活跃次数或匹配规则设置线索分级
        String leadLevel = determineLeadLevel(lead, config);
        highPotentialLead.setLeadLevel(leadLevel);
        
        highPotentialLead.setCreateTime(LocalDateTime.now());
        highPotentialLead.setUpdateTime(LocalDateTime.now());
        return highPotentialLead;
    }
    
    /**
     * 确定匹配规则
     */
    private String determineMatchingRule(Lead lead, MonitorIndicator config) {
        // 检查活跃次数
        if (activityDataService != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = now.minusDays(config.getTimeRangeDays());
            List<String> orgList = new ArrayList<>(parseOrganizations(config.getActiveOrganizations()));
            int activityCount = activityDataService.getActivityCount(lead.getId(), startTime, now, orgList);
            
            if (activityCount >= config.getActivityThreshold()) {
                return "活跃次数达标（" + activityCount + "次）";
            }
        }
        
        // 检查活跃机构匹配
        Set<String> activeOrganizations = parseOrganizations(config.getActiveOrganizations());
        String sourceChannel = lead.getSourceChannel();
        if (sourceChannel != null && activeOrganizations.contains(sourceChannel)) {
            return "活跃机构命中（" + sourceChannel + "）";
        }
        
        // 新线索
        if ("新线索".equals(lead.getStatus())) {
            return "新线索优先";
        }
        
        return "其他规则命中";
    }
    
    /**
     * 确定线索分级
     */
    private String determineLeadLevel(Lead lead, MonitorIndicator config) {
        // 根据活跃次数分级
        if (activityDataService != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = now.minusDays(config.getTimeRangeDays());
            List<String> orgList = new ArrayList<>(parseOrganizations(config.getActiveOrganizations()));
            int activityCount = activityDataService.getActivityCount(lead.getId(), startTime, now, orgList);
            
            if (activityCount >= 10) {
                return "A"; // 高潜
            } else if (activityCount >= 5) {
                return "B"; // 中潜
            } else {
                return "C"; // 低潜
            }
        }
        
        // 默认分级
        return "B";
    }
    
    /**
     * 解析活跃机构JSON字符串
     */
    private Set<String> parseOrganizations(String organizationsJson) {
        Set<String> organizations = new HashSet<>();
        if (organizationsJson != null && !organizationsJson.isEmpty()) {
            try {
                List<String> orgList = JSON.parseArray(organizationsJson, String.class);
                if (orgList != null) {
                    organizations.addAll(orgList);
                }
            } catch (Exception e) {
                log.warn("解析活跃机构配置失败: {}", organizationsJson, e);
            }
        }
        return organizations;
    }
}


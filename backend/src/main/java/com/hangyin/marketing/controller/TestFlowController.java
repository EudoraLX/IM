package com.hangyin.marketing.controller;

import com.hangyin.marketing.common.Result;
import com.hangyin.marketing.entity.Lead;
import com.hangyin.marketing.entity.MonitorIndicator;
import com.hangyin.marketing.entity.PushFrequency;
import com.hangyin.marketing.mapper.HighPotentialLeadMapper;
import com.hangyin.marketing.mapper.LeadMapper;
import com.hangyin.marketing.service.DataProcessingService;
import com.hangyin.marketing.service.LeadService;
import com.hangyin.marketing.service.MonitorIndicatorService;
import com.hangyin.marketing.service.PushFrequencyService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 完整测试流程控制器
 * 用于可视化展示四个模块的串联流程
 */
@Slf4j
@RestController
@RequestMapping("/testFlow")
@CrossOrigin
public class TestFlowController {
    
    @Autowired
    private LeadService leadService;
    
    @Autowired
    private MonitorIndicatorService monitorIndicatorService;
    
    @Autowired
    private PushFrequencyService pushFrequencyService;
    
    @Autowired
    private DataProcessingService dataProcessingService;
    
    @Autowired
    private LeadMapper leadMapper;
    
    @Autowired
    private HighPotentialLeadMapper highPotentialLeadMapper;
    
    /**
     * 获取当前系统状态（用于可视化展示）
     */
    @GetMapping("/status")
    public Result<FlowStatus> getFlowStatus() {
        try {
            FlowStatus status = new FlowStatus();
            
            // 1. 线索管理模块状态
            QueryWrapper<Lead> leadWrapper = new QueryWrapper<>();
            leadWrapper.eq("deleted", 0);
            long leadCount = leadMapper.selectCount(leadWrapper);
            status.setLeadCount(leadCount);
            status.setLeadStatus(leadCount > 0 ? "正常" : "无数据");
            
            // 2. 监控指标管理模块状态
            MonitorIndicator monitorConfig = monitorIndicatorService.getActiveConfig();
            if (monitorConfig != null) {
                status.setMonitorConfigExists(true);
                status.setMonitorTimeRange(monitorConfig.getTimeRangeDays() + "天");
                status.setMonitorThreshold(monitorConfig.getActivityThreshold());
                status.setMonitorOrganizations(monitorConfig.getActiveOrganizations());
                status.setMonitorStatus("已配置");
            } else {
                status.setMonitorConfigExists(false);
                status.setMonitorStatus("未配置");
            }
            
            // 3. 推送频率管理模块状态
            PushFrequency pushConfig = pushFrequencyService.getActiveConfig();
            if (pushConfig != null) {
                status.setPushConfigExists(true);
                status.setPushFrequency(pushConfig.getFrequencyType());
                status.setPushInterval(pushConfig.getPushInterval() + "分钟");
                status.setPushStatus("已配置");
            } else {
                status.setPushConfigExists(false);
                status.setPushStatus("未配置");
            }
            
            // 4. 高潜线索营销模块状态
            QueryWrapper<com.hangyin.marketing.entity.HighPotentialLead> hplWrapper = new QueryWrapper<>();
            long highPotentialCount = highPotentialLeadMapper.selectCount(hplWrapper);
            status.setHighPotentialCount(highPotentialCount);
            status.setHighPotentialStatus(highPotentialCount > 0 ? "有数据" : "无数据");
            
            // 5. 流程完整性判断
            boolean flowComplete = status.getLeadCount() > 0 
                    && status.isMonitorConfigExists() 
                    && status.isPushConfigExists();
            status.setFlowComplete(flowComplete);
            
            return Result.success(status);
        } catch (Exception e) {
            log.error("获取流程状态失败", e);
            return Result.error("获取状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行完整测试流程
     */
    @PostMapping("/execute")
    public Result<FlowExecutionResult> executeFullFlow(@RequestParam(defaultValue = "20") Integer leadCount) {
        FlowExecutionResult result = new FlowExecutionResult();
        List<FlowStep> steps = new ArrayList<>();
        
        try {
            // 步骤1：创建测试线索
            FlowStep step1 = new FlowStep();
            step1.setStepNumber(1);
            step1.setStepName("创建测试线索");
            step1.setModuleName("线索管理");
            step1.setStatus("执行中");
            steps.add(step1);
            
            List<Lead> testLeads = new ArrayList<>();
            for (int i = 1; i <= leadCount / 2; i++) {
                Lead lead = new Lead();
                lead.setCustomerName("高潜客户" + i);
                lead.setContactPhone("138" + String.format("%08d", i));
                lead.setSourceChannel("华东中心机构");
                lead.setStatus("新线索");
                testLeads.add(lead);
            }
            for (int i = leadCount / 2 + 1; i <= leadCount; i++) {
                Lead lead = new Lead();
                lead.setCustomerName("普通客户" + i);
                lead.setContactPhone("139" + String.format("%08d", i));
                lead.setSourceChannel("其他渠道");
                lead.setStatus("新线索");
                testLeads.add(lead);
            }
            leadService.batchImportLeads(testLeads);
            step1.setStatus("完成");
            step1.setMessage("成功创建 " + leadCount + " 条测试线索");
            result.setLeadCreated(leadCount);
            
            // 步骤2：检查/创建监控指标配置
            FlowStep step2 = new FlowStep();
            step2.setStepNumber(2);
            step2.setStepName("配置监控指标");
            step2.setModuleName("监控指标管理");
            step2.setStatus("执行中");
            steps.add(step2);
            
            MonitorIndicator monitorConfig = monitorIndicatorService.getActiveConfig();
            if (monitorConfig == null) {
                // 创建默认配置
                MonitorIndicator newConfig = new MonitorIndicator();
                newConfig.setTimeRangeDays(7);
                newConfig.setActiveOrganizations("[\"华东中心机构\", \"上海研发分部\"]");
                newConfig.setActivityThreshold(3);
                newConfig.setStatus(1);
                monitorIndicatorService.saveOrUpdateConfig(newConfig);
                step2.setMessage("已创建默认监控指标配置");
            } else {
                step2.setMessage("使用现有监控指标配置");
            }
            step2.setStatus("完成");
            result.setMonitorConfigured(true);
            
            // 步骤3：检查/创建推送频率配置
            FlowStep step3 = new FlowStep();
            step3.setStepNumber(3);
            step3.setStepName("配置推送频率");
            step3.setModuleName("推送频率管理");
            step3.setStatus("执行中");
            steps.add(step3);
            
            PushFrequency pushConfig = pushFrequencyService.getActiveConfig();
            if (pushConfig == null) {
                // 创建默认配置
                PushFrequency newPushConfig = new PushFrequency();
                newPushConfig.setFrequencyType("daily");
                newPushConfig.setPushInterval(15);
                newPushConfig.setWeekendRule(1);
                newPushConfig.setMinPushThreshold(30);
                newPushConfig.setStatus(1);
                pushFrequencyService.saveOrUpdateConfig(newPushConfig);
                step3.setMessage("已创建默认推送频率配置");
            } else {
                step3.setMessage("使用现有推送频率配置");
            }
            step3.setStatus("完成");
            result.setPushConfigured(true);
            
            // 步骤4：执行数据加工，筛选高潜线索
            FlowStep step4 = new FlowStep();
            step4.setStepNumber(4);
            step4.setStepName("数据加工与筛选");
            step4.setModuleName("数据加工及清洗");
            step4.setStatus("执行中");
            steps.add(step4);
            
            int highPotentialCount = dataProcessingService.processDataAndFilterHighPotentialLeads();
            step4.setStatus("完成");
            step4.setMessage("筛选出 " + highPotentialCount + " 条高潜线索");
            result.setHighPotentialFiltered(highPotentialCount);
            
            // 步骤5：准备营销数据
            FlowStep step5 = new FlowStep();
            step5.setStepNumber(5);
            step5.setStepName("准备营销数据");
            step5.setModuleName("高潜线索营销");
            step5.setStatus("完成");
            step5.setMessage("高潜线索已准备就绪，可在'高潜线索营销'页面查看和操作");
            steps.add(step5);
            
            result.setSteps(steps);
            result.setSuccess(true);
            result.setMessage("完整测试流程执行成功！");
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("执行完整流程失败", e);
            result.setSuccess(false);
            result.setMessage("执行失败: " + e.getMessage());
            result.setSteps(steps);
            return Result.error("执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 流程状态数据类
     */
    @Data
    public static class FlowStatus {
        // 线索管理
        private long leadCount;
        private String leadStatus;
        
        // 监控指标管理
        private boolean monitorConfigExists;
        private String monitorStatus;
        private String monitorTimeRange;
        private Integer monitorThreshold;
        private String monitorOrganizations;
        
        // 推送频率管理
        private boolean pushConfigExists;
        private String pushStatus;
        private String pushFrequency;
        private String pushInterval;
        
        // 高潜线索营销
        private long highPotentialCount;
        private String highPotentialStatus;
        
        // 流程完整性
        private boolean flowComplete;
    }
    
    /**
     * 流程执行结果
     */
    @Data
    public static class FlowExecutionResult {
        private boolean success;
        private String message;
        private List<FlowStep> steps;
        private Integer leadCreated;
        private Boolean monitorConfigured;
        private Boolean pushConfigured;
        private Integer highPotentialFiltered;
    }
    
    /**
     * 流程步骤
     */
    @Data
    public static class FlowStep {
        private Integer stepNumber;
        private String stepName;
        private String moduleName;
        private String status; // 执行中、完成、失败
        private String message;
    }
}


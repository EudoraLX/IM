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
     * 获取测试步骤预览（不执行，仅返回步骤信息）
     */
    @GetMapping("/preview")
    public Result<List<FlowStep>> getFlowPreview(@RequestParam(defaultValue = "20") Integer leadCount) {
        try {
            List<FlowStep> steps = new ArrayList<>();
            
            // 步骤1：创建测试线索
            FlowStep step1 = new FlowStep();
            step1.setStepNumber(1);
            step1.setStepName("创建测试线索");
            step1.setModuleName("线索管理");
            step1.setStatus("待执行");
            step1.setMessage("将创建 " + leadCount + " 条测试线索（其中 " + (leadCount / 2) + " 条高潜客户，"
                    + (leadCount - leadCount / 2) + " 条普通客户）");
            steps.add(step1);
            
            // 步骤2：检查/创建监控指标配置
            FlowStep step2 = new FlowStep();
            step2.setStepNumber(2);
            step2.setStepName("配置监控指标");
            step2.setModuleName("监控指标管理");
            step2.setStatus("待执行");
            MonitorIndicator monitorConfig = monitorIndicatorService.getActiveConfig();
            if (monitorConfig == null) {
                step2.setMessage("将创建默认监控指标配置（时间范围：7天，活跃阈值：3，机构：华东中心机构、上海研发分部）");
            } else {
                step2.setMessage("将使用现有监控指标配置（时间范围：" + monitorConfig.getTimeRangeDays() 
                        + "天，活跃阈值：" + monitorConfig.getActivityThreshold() + "）");
            }
            steps.add(step2);
            
            // 步骤3：检查/创建推送频率配置
            FlowStep step3 = new FlowStep();
            step3.setStepNumber(3);
            step3.setStepName("配置推送频率");
            step3.setModuleName("推送频率管理");
            step3.setStatus("待执行");
            PushFrequency pushConfig = pushFrequencyService.getActiveConfig();
            if (pushConfig == null) {
                step3.setMessage("将创建默认推送频率配置（频率类型：daily，推送间隔：15分钟）");
            } else {
                step3.setMessage("将使用现有推送频率配置（频率类型：" + pushConfig.getFrequencyType() 
                        + "，推送间隔：" + pushConfig.getPushInterval() + "分钟）");
            }
            steps.add(step3);
            
            // 步骤4：执行数据加工，筛选高潜线索
            FlowStep step4 = new FlowStep();
            step4.setStepNumber(4);
            step4.setStepName("数据加工与筛选");
            step4.setModuleName("数据加工及清洗");
            step4.setStatus("待执行");
            
            MonitorIndicator monitorConfigForStep4 = monitorIndicatorService.getActiveConfig();
            StringBuilder step4Message = new StringBuilder();
            step4Message.append("实际执行代码：\n");
            step4Message.append("```java\n");
            step4Message.append("@Override\n");
            step4Message.append("@Transactional\n");
            step4Message.append("public int processDataAndFilterHighPotentialLeads() {\n");
            step4Message.append("    // 1. 获取当前启用的监控指标配置\n");
            step4Message.append("    MonitorIndicator config = monitorIndicatorService.getActiveConfig();\n");
            if (monitorConfigForStep4 != null) {
                step4Message.append("    // 配置参数：时间范围=").append(monitorConfigForStep4.getTimeRangeDays())
                        .append("天, 阈值=").append(monitorConfigForStep4.getActivityThreshold()).append("次\n");
            }
            step4Message.append("    \n");
            step4Message.append("    // 2. 获取所有未删除的线索\n");
            step4Message.append("    QueryWrapper<Lead> leadWrapper = new QueryWrapper<>();\n");
            step4Message.append("    leadWrapper.eq(\"deleted\", 0);\n");
            step4Message.append("    List<Lead> allLeads = leadMapper.selectList(leadWrapper);\n");
            step4Message.append("    \n");
            step4Message.append("    // 3. 解析监控配置\n");
            step4Message.append("    Integer timeRangeDays = config.getTimeRangeDays();\n");
            step4Message.append("    Integer activityThreshold = config.getActivityThreshold();\n");
            step4Message.append("    Set<String> activeOrganizations = parseOrganizations(\n");
            step4Message.append("        config.getActiveOrganizations());\n");
            step4Message.append("    LocalDateTime now = LocalDateTime.now();\n");
            step4Message.append("    LocalDateTime startTime = now.minusDays(timeRangeDays);\n");
            step4Message.append("    \n");
            step4Message.append("    // 4. 遍历线索，判断是否为高潜线索\n");
            step4Message.append("    for (Lead lead : allLeads) {\n");
            step4Message.append("        if (isHighPotentialLead(lead, config, startTime, now)) {\n");
            step4Message.append("            // 创建高潜线索记录\n");
            step4Message.append("            HighPotentialLead hpl = createHighPotentialLead(lead, config);\n");
            step4Message.append("            highPotentialLeadMapper.insert(hpl);\n");
            step4Message.append("            highPotentialCount++;\n");
            step4Message.append("        }\n");
            step4Message.append("    }\n");
            step4Message.append("    return highPotentialCount;\n");
            step4Message.append("}\n");
            step4Message.append("\n");
            step4Message.append("// 判断高潜线索的核心方法\n");
            step4Message.append("private boolean isHighPotentialLead(Lead lead, MonitorIndicator config,\n");
            step4Message.append("        LocalDateTime startTime, LocalDateTime endTime) {\n");
            step4Message.append("    // 判断1：活跃次数 >= 阈值（核心判断）\n");
            step4Message.append("    int activityCount = activityDataService.getActivityCount(\n");
            step4Message.append("        lead.getId(), startTime, endTime, orgList);\n");
            step4Message.append("    if (activityCount >= activityThreshold) {\n");
            step4Message.append("        return true;\n");
            step4Message.append("    }\n");
            step4Message.append("    \n");
            step4Message.append("    // 判断2：活跃机构匹配\n");
            step4Message.append("    if (activeOrganizations.contains(lead.getSourceChannel())) {\n");
            step4Message.append("        return true;\n");
            step4Message.append("    }\n");
            step4Message.append("    \n");
            step4Message.append("    // 判断3：新线索且创建时间在监控范围内\n");
            step4Message.append("    if (\"新线索\".equals(lead.getStatus()) && \n");
            step4Message.append("        lead.getCreateTime().isAfter(startTime)) {\n");
            step4Message.append("        return true;\n");
            step4Message.append("    }\n");
            step4Message.append("    return false;\n");
            step4Message.append("}\n");
            step4Message.append("\n");
            step4Message.append("// 创建高潜线索记录\n");
            step4Message.append("private HighPotentialLead createHighPotentialLead(Lead lead, MonitorIndicator config) {\n");
            step4Message.append("    HighPotentialLead hpl = new HighPotentialLead();\n");
            step4Message.append("    hpl.setLeadId(lead.getId());\n");
            step4Message.append("    hpl.setUserId(\"USER_\" + lead.getId());\n");
            step4Message.append("    hpl.setMatchingRule(determineMatchingRule(lead, config));\n");
            step4Message.append("    hpl.setStatus(\"待营销处理\");\n");
            step4Message.append("    hpl.setLeadLevel(determineLeadLevel(lead, config));\n");
            step4Message.append("    hpl.setMatchingDate(LocalDateTime.now());\n");
            step4Message.append("    return hpl;\n");
            step4Message.append("}\n");
            step4Message.append("```");
            step4.setMessage(step4Message.toString());
            steps.add(step4);
            
            // 步骤5：准备营销数据
            FlowStep step5 = new FlowStep();
            step5.setStepNumber(5);
            step5.setStepName("准备营销数据");
            step5.setModuleName("高潜线索营销");
            step5.setStatus("待执行");
            
            StringBuilder step5Message = new StringBuilder();
            step5Message.append("数据已准备就绪，相关查询代码：\n");
            step5Message.append("```java\n");
            step5Message.append("// 1. 分页查询高潜线索列表（HighPotentialLeadController）\n");
            step5Message.append("@GetMapping(\"/list\")\n");
            step5Message.append("public Result<PageResult<HighPotentialLead>> getHighPotentialLeadList(\n");
            step5Message.append("        @RequestParam Integer current,\n");
            step5Message.append("        @RequestParam Integer size,\n");
            step5Message.append("        @RequestParam(required = false) String keyword,\n");
            step5Message.append("        @RequestParam(required = false) String status) {\n");
            step5Message.append("    PageRequest pageRequest = new PageRequest();\n");
            step5Message.append("    pageRequest.setCurrent(current);\n");
            step5Message.append("    pageRequest.setSize(size);\n");
            step5Message.append("    PageResult<HighPotentialLead> result = \n");
            step5Message.append("        highPotentialLeadService.getHighPotentialLeadList(\n");
            step5Message.append("            pageRequest, keyword, status);\n");
            step5Message.append("    return Result.success(result);\n");
            step5Message.append("}\n");
            step5Message.append("\n");
            step5Message.append("// 2. SQL查询（HighPotentialLeadMapper.xml）\n");
            step5Message.append("SELECT hpl.*, l.customer_name, l.contact_phone\n");
            step5Message.append("FROM high_potential_lead hpl\n");
            step5Message.append("LEFT JOIN `lead` l ON hpl.lead_id = l.id\n");
            step5Message.append("WHERE 1=1\n");
            step5Message.append("  AND hpl.status = #{status}\n");
            step5Message.append("ORDER BY hpl.matching_date DESC\n");
            step5Message.append("LIMIT #{offset}, #{size}\n");
            step5Message.append("\n");
            step5Message.append("// 3. 获取统计信息\n");
            step5Message.append("@GetMapping(\"/statistics\")\n");
            step5Message.append("public Result<Map<String, Object>> getStatistics() {\n");
            step5Message.append("    Map<String, Object> stats = new HashMap<>();\n");
            step5Message.append("    stats.put(\"totalMatched\", countHighPotentialLeads(null, null));\n");
            step5Message.append("    stats.put(\"todayNew\", countTodayNewLeads());\n");
            step5Message.append("    stats.put(\"pendingCount\", countHighPotentialLeads(null, \"待营销处理\"));\n");
            step5Message.append("    stats.put(\"conversionRate\", calculateConversionRate());\n");
            step5Message.append("    return Result.success(stats);\n");
            step5Message.append("}\n");
            step5Message.append("\n");
            step5Message.append("// 4. 批量外呼\n");
            step5Message.append("@PostMapping(\"/batchCall\")\n");
            step5Message.append("public Result<Void> batchCall(@RequestBody List<Long> ids) {\n");
            step5Message.append("    highPotentialLeadService.batchCall(ids);\n");
            step5Message.append("    return Result.success();\n");
            step5Message.append("}\n");
            step5Message.append("\n");
            step5Message.append("// 5. 批量发送短信\n");
            step5Message.append("@PostMapping(\"/batchSendSms\")\n");
            step5Message.append("public Result<Void> batchSendSms(@RequestBody List<Long> ids) {\n");
            step5Message.append("    highPotentialLeadService.batchSendSms(ids);\n");
            step5Message.append("    return Result.success();\n");
            step5Message.append("}\n");
            step5Message.append("\n");
            step5Message.append("// 6. 更新线索状态\n");
            step5Message.append("@PutMapping(\"/{id}/status\")\n");
            step5Message.append("public Result<Void> updateStatus(@PathVariable Long id,\n");
            step5Message.append("        @RequestParam String status) {\n");
            step5Message.append("    highPotentialLeadService.updateLeadStatus(id, status);\n");
            step5Message.append("    return Result.success();\n");
            step5Message.append("}\n");
            step5Message.append("```");
            step5.setMessage(step5Message.toString());
            steps.add(step5);
            
            return Result.success(steps);
        } catch (Exception e) {
            log.error("获取测试步骤预览失败", e);
            return Result.error("获取预览失败: " + e.getMessage());
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
            
            MonitorIndicator executedConfig = monitorIndicatorService.getActiveConfig();
            StringBuilder step4ExecMessage = new StringBuilder();
            step4ExecMessage.append("执行完成，实际执行代码：\n");
            step4ExecMessage.append("```java\n");
            step4ExecMessage.append("@Override\n");
            step4ExecMessage.append("@Transactional\n");
            step4ExecMessage.append("public int processDataAndFilterHighPotentialLeads() {\n");
            step4ExecMessage.append("    // ✓ 1. 获取监控指标配置\n");
            step4ExecMessage.append("    MonitorIndicator config = monitorIndicatorService.getActiveConfig();\n");
            if (executedConfig != null) {
                step4ExecMessage.append("    // 实际配置：时间范围=").append(executedConfig.getTimeRangeDays())
                        .append("天, 阈值=").append(executedConfig.getActivityThreshold()).append("次\n");
            }
            step4ExecMessage.append("    \n");
            step4ExecMessage.append("    // ✓ 2. 获取所有未删除的线索\n");
            step4ExecMessage.append("    QueryWrapper<Lead> leadWrapper = new QueryWrapper<>();\n");
            step4ExecMessage.append("    leadWrapper.eq(\"deleted\", 0);\n");
            step4ExecMessage.append("    List<Lead> allLeads = leadMapper.selectList(leadWrapper);\n");
            step4ExecMessage.append("    \n");
            step4ExecMessage.append("    // ✓ 3. 解析配置并遍历线索\n");
            step4ExecMessage.append("    LocalDateTime now = LocalDateTime.now();\n");
            step4ExecMessage.append("    LocalDateTime startTime = now.minusDays(timeRangeDays);\n");
            step4ExecMessage.append("    \n");
            step4ExecMessage.append("    for (Lead lead : allLeads) {\n");
            step4ExecMessage.append("        if (isHighPotentialLead(lead, config, startTime, now)) {\n");
            step4ExecMessage.append("            HighPotentialLead hpl = createHighPotentialLead(lead, config);\n");
            step4ExecMessage.append("            highPotentialLeadMapper.insert(hpl);\n");
            step4ExecMessage.append("            highPotentialCount++;\n");
            step4ExecMessage.append("        }\n");
            step4ExecMessage.append("    }\n");
            step4ExecMessage.append("    \n");
            step4ExecMessage.append("    return highPotentialCount; // 返回: ").append(highPotentialCount).append(" 条\n");
            step4ExecMessage.append("}\n");
            step4ExecMessage.append("```\n");
            step4ExecMessage.append("执行结果：成功筛选出 ").append(highPotentialCount).append(" 条高潜线索");
            step4.setMessage(step4ExecMessage.toString());
            result.setHighPotentialFiltered(highPotentialCount);
            
            // 步骤5：准备营销数据
            FlowStep step5 = new FlowStep();
            step5.setStepNumber(5);
            step5.setStepName("准备营销数据");
            step5.setModuleName("高潜线索营销");
            step5.setStatus("完成");
            
            StringBuilder step5ExecMessage = new StringBuilder();
            step5ExecMessage.append("数据准备完成，相关查询代码：\n");
            step5ExecMessage.append("```java\n");
            step5ExecMessage.append("// ✓ 高潜线索已写入 high_potential_lead 表（共 ").append(highPotentialCount).append(" 条）\n");
            step5ExecMessage.append("\n");
            step5ExecMessage.append("// 1. 分页查询高潜线索列表\n");
            step5ExecMessage.append("@GetMapping(\"/highPotential/list\")\n");
            step5ExecMessage.append("public Result<PageResult<HighPotentialLead>> getHighPotentialLeadList(\n");
            step5ExecMessage.append("        @RequestParam Integer current,\n");
            step5ExecMessage.append("        @RequestParam Integer size) {\n");
            step5ExecMessage.append("    PageResult<HighPotentialLead> result = \n");
            step5ExecMessage.append("        highPotentialLeadService.getHighPotentialLeadList(\n");
            step5ExecMessage.append("            pageRequest, keyword, status);\n");
            step5ExecMessage.append("    return Result.success(result);\n");
            step5ExecMessage.append("}\n");
            step5ExecMessage.append("\n");
            step5ExecMessage.append("// 2. SQL查询（关联lead表获取客户信息）\n");
            step5ExecMessage.append("SELECT hpl.*, l.customer_name, l.contact_phone\n");
            step5ExecMessage.append("FROM high_potential_lead hpl\n");
            step5ExecMessage.append("LEFT JOIN `lead` l ON hpl.lead_id = l.id\n");
            step5ExecMessage.append("WHERE hpl.status = '待营销处理'\n");
            step5ExecMessage.append("ORDER BY hpl.matching_date DESC\n");
            step5ExecMessage.append("\n");
            step5ExecMessage.append("// 3. 获取统计信息\n");
            step5ExecMessage.append("@GetMapping(\"/highPotential/statistics\")\n");
            step5ExecMessage.append("public Result<Map<String, Object>> getStatistics() {\n");
            step5ExecMessage.append("    Map<String, Object> stats = new HashMap<>();\n");
            step5ExecMessage.append("    stats.put(\"totalMatched\", countHighPotentialLeads(null, null));\n");
            step5ExecMessage.append("    stats.put(\"todayNew\", countTodayNewLeads());\n");
            step5ExecMessage.append("    stats.put(\"pendingCount\", countHighPotentialLeads(null, \"待营销处理\"));\n");
            step5ExecMessage.append("    return Result.success(stats);\n");
            step5ExecMessage.append("}\n");
            step5ExecMessage.append("\n");
            step5ExecMessage.append("// 4. 批量外呼\n");
            step5ExecMessage.append("@PostMapping(\"/highPotential/batchCall\")\n");
            step5ExecMessage.append("public Result<Void> batchCall(@RequestBody List<Long> ids) {\n");
            step5ExecMessage.append("    highPotentialLeadService.batchCall(ids);\n");
            step5ExecMessage.append("    return Result.success();\n");
            step5ExecMessage.append("}\n");
            step5ExecMessage.append("\n");
            step5ExecMessage.append("// 5. 批量发送短信\n");
            step5ExecMessage.append("@PostMapping(\"/highPotential/batchSendSms\")\n");
            step5ExecMessage.append("public Result<Void> batchSendSms(@RequestBody List<Long> ids) {\n");
            step5ExecMessage.append("    highPotentialLeadService.batchSendSms(ids);\n");
            step5ExecMessage.append("    return Result.success();\n");
            step5ExecMessage.append("}\n");
            step5ExecMessage.append("```\n");
            step5ExecMessage.append("执行结果：").append(highPotentialCount).append(" 条高潜线索已准备就绪，可在'高潜线索营销'页面查看和操作");
            step5.setMessage(step5ExecMessage.toString());
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


package com.hangyin.marketing.controller;

import com.hangyin.marketing.common.Result;
import com.hangyin.marketing.entity.Lead;
import com.hangyin.marketing.service.DataProcessingService;
import com.hangyin.marketing.service.LeadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试数据控制器
 * 用于创建测试数据，模拟高潜客户
 */
@Slf4j
@RestController
@RequestMapping("/test")
@CrossOrigin
public class TestDataController {
    
    @Autowired
    private LeadService leadService;
    
    @Autowired
    private DataProcessingService dataProcessingService;
    
    /**
     * 创建测试线索数据（包含高潜线索）
     */
    @PostMapping("/createTestLeads")
    public Result<String> createTestLeads(@RequestParam(defaultValue = "10") Integer count) {
        try {
            List<Lead> testLeads = new ArrayList<>();
            
            // 创建高潜线索（来源渠道匹配活跃机构）
            for (int i = 1; i <= count / 2; i++) {
                Lead lead = new Lead();
                lead.setCustomerName("高潜客户" + i);
                lead.setContactPhone("138" + String.format("%08d", i));
                lead.setSourceChannel("华东中心机构"); // 匹配监控指标中的活跃机构
                lead.setStatus("新线索");
                testLeads.add(lead);
            }
            
            // 创建普通线索
            for (int i = count / 2 + 1; i <= count; i++) {
                Lead lead = new Lead();
                lead.setCustomerName("普通客户" + i);
                lead.setContactPhone("139" + String.format("%08d", i));
                lead.setSourceChannel("其他渠道");
                lead.setStatus("新线索");
                testLeads.add(lead);
            }
            
            // 批量导入
            leadService.batchImportLeads(testLeads);
            
            return Result.success("成功创建 " + count + " 条测试线索，其中 " + (count / 2) + " 条为高潜线索");
        } catch (Exception e) {
            log.error("创建测试线索失败", e);
            return Result.error("创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动触发数据加工，筛选高潜线索
     */
    @PostMapping("/processData")
    public Result<Integer> processData() {
        try {
            int count = dataProcessingService.processDataAndFilterHighPotentialLeads();
            return Result.success("数据加工完成，筛选出 " + count + " 条高潜线索", count);
        } catch (Exception e) {
            log.error("执行数据加工失败", e);
            return Result.error("执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 一键创建测试数据并执行数据加工
     */
    @PostMapping("/setupTestData")
    public Result<String> setupTestData(@RequestParam(defaultValue = "10") Integer leadCount) {
        try {
            // 1. 创建测试线索
            List<Lead> testLeads = new ArrayList<>();
            
            // 创建高潜线索（来源渠道匹配活跃机构）
            for (int i = 1; i <= leadCount / 2; i++) {
                Lead lead = new Lead();
                lead.setCustomerName("高潜客户" + i);
                lead.setContactPhone("138" + String.format("%08d", i));
                lead.setSourceChannel("华东中心机构"); // 匹配监控指标中的活跃机构
                lead.setStatus("新线索");
                testLeads.add(lead);
            }
            
            // 创建普通线索
            for (int i = leadCount / 2 + 1; i <= leadCount; i++) {
                Lead lead = new Lead();
                lead.setCustomerName("普通客户" + i);
                lead.setContactPhone("139" + String.format("%08d", i));
                lead.setSourceChannel("其他渠道");
                lead.setStatus("新线索");
                testLeads.add(lead);
            }
            
            leadService.batchImportLeads(testLeads);
            log.info("已创建 {} 条测试线索", leadCount);
            
            // 2. 执行数据加工
            int highPotentialCount = dataProcessingService.processDataAndFilterHighPotentialLeads();
            log.info("已筛选出 {} 条高潜线索", highPotentialCount);
            
            return Result.success("测试数据创建完成！\n" +
                    "- 创建线索: " + leadCount + " 条\n" +
                    "- 筛选高潜线索: " + highPotentialCount + " 条\n" +
                    "请在'高潜线索营销'页面查看结果");
        } catch (Exception e) {
            log.error("创建测试数据失败", e);
            return Result.error("创建失败: " + e.getMessage());
        }
    }
}


package com.hangyin.marketing.controller;

import com.hangyin.marketing.common.Result;
import com.hangyin.marketing.service.DataProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据加工控制器
 * 提供手动触发数据加工的接口
 */
@Slf4j
@RestController
@RequestMapping("/dataProcessing")
@CrossOrigin
public class DataProcessingController {
    
    @Autowired
    private DataProcessingService dataProcessingService;
    
    /**
     * 手动触发数据加工任务
     */
    @PostMapping("/process")
    public Result<Integer> processData() {
        try {
            int count = dataProcessingService.processDataAndFilterHighPotentialLeads();
            return Result.success("数据加工完成，筛选出 " + count + " 条高潜线索", count);
        } catch (Exception e) {
            log.error("执行数据加工失败", e);
            return Result.error("执行数据加工失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量处理指定线索
     */
    @PostMapping("/batchProcess")
    public Result<Integer> batchProcessLeads(@RequestBody List<Long> leadIds) {
        try {
            int count = dataProcessingService.batchProcessLeads(leadIds);
            return Result.success("批量处理完成，筛选出 " + count + " 条高潜线索", count);
        } catch (Exception e) {
            log.error("批量处理线索失败", e);
            return Result.error("批量处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查单个线索是否为高潜线索
     */
    @GetMapping("/check/{leadId}")
    public Result<Boolean> checkHighPotential(@PathVariable Long leadId) {
        try {
            boolean isHighPotential = dataProcessingService.isHighPotentialLead(leadId);
            return Result.success(isHighPotential);
        } catch (Exception e) {
            log.error("检查高潜线索失败", e);
            return Result.error("检查失败: " + e.getMessage());
        }
    }
}


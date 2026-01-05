package com.hangyin.marketing.controller;

import com.hangyin.marketing.common.Result;
import com.hangyin.marketing.entity.MonitorIndicator;
import com.hangyin.marketing.service.MonitorIndicatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 监控指标管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/monitor")
@CrossOrigin
public class MonitorIndicatorController {
    
    @Autowired
    private MonitorIndicatorService monitorIndicatorService;
    
    /**
     * 获取当前监控指标配置
     */
    @GetMapping("/config")
    public Result<MonitorIndicator> getConfig() {
        MonitorIndicator config = monitorIndicatorService.getActiveConfig();
        return Result.success(config);
    }
    
    /**
     * 保存或更新监控指标配置
     */
    @PostMapping("/save")
    public Result<Void> saveConfig(@RequestBody MonitorIndicator indicator) {
        monitorIndicatorService.saveOrUpdateConfig(indicator);
        return Result.success();
    }
    
    /**
     * 获取实时监控统计数据（用于图表展示）
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = monitorIndicatorService.getStatistics();
        return Result.success(statistics);
    }
}

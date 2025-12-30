package com.hangyin.marketing.controller;

import com.hangyin.marketing.common.Result;
import com.hangyin.marketing.entity.PushFrequency;
import com.hangyin.marketing.service.PushFrequencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 推送频率管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/push")
@CrossOrigin
public class PushFrequencyController {
    
    @Autowired
    private PushFrequencyService pushFrequencyService;
    
    /**
     * 获取当前推送频率配置
     */
    @GetMapping("/config")
    public Result<PushFrequency> getConfig() {
        PushFrequency config = pushFrequencyService.getActiveConfig();
        return Result.success(config);
    }
    
    /**
     * 保存或更新推送频率配置
     */
    @PostMapping("/save")
    public Result<Void> saveConfig(@RequestBody PushFrequency pushFrequency) {
        pushFrequencyService.saveOrUpdateConfig(pushFrequency);
        return Result.success();
    }
    
    /**
     * 重置配置
     */
    @PostMapping("/reset")
    public Result<Void> resetConfig() {
        pushFrequencyService.resetConfig();
        return Result.success();
    }
}


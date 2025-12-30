package com.hangyin.marketing.service;

import com.hangyin.marketing.entity.MonitorIndicator;

/**
 * 监控指标服务接口
 */
public interface MonitorIndicatorService {
    
    /**
     * 获取当前启用的监控指标配置
     */
    MonitorIndicator getActiveConfig();
    
    /**
     * 保存或更新监控指标配置
     */
    void saveOrUpdateConfig(MonitorIndicator indicator);
}


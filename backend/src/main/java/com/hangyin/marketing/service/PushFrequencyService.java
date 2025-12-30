package com.hangyin.marketing.service;

import com.hangyin.marketing.entity.PushFrequency;

/**
 * 推送频率服务接口
 */
public interface PushFrequencyService {
    
    /**
     * 获取当前启用的推送频率配置
     */
    PushFrequency getActiveConfig();
    
    /**
     * 保存或更新推送频率配置
     */
    void saveOrUpdateConfig(PushFrequency pushFrequency);
    
    /**
     * 重置配置
     */
    void resetConfig();
}


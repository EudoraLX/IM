package com.hangyin.marketing.service.impl;

import com.hangyin.marketing.entity.PushFrequency;
import com.hangyin.marketing.mapper.PushFrequencyMapper;
import com.hangyin.marketing.service.PushFrequencyService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 推送频率服务实现类
 */
@Slf4j
@Service
public class PushFrequencyServiceImpl implements PushFrequencyService {
    
    @Autowired
    private PushFrequencyMapper pushFrequencyMapper;
    
    @Override
    public PushFrequency getActiveConfig() {
        QueryWrapper<PushFrequency> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.orderByDesc("update_time");
        wrapper.last("LIMIT 1");
        return pushFrequencyMapper.selectOne(wrapper);
    }
    
    @Override
    @Transactional
    public void saveOrUpdateConfig(PushFrequency pushFrequency) {
        if (pushFrequency.getId() == null) {
            pushFrequency.setCreateTime(LocalDateTime.now());
            pushFrequency.setUpdateTime(LocalDateTime.now());
            pushFrequency.setStatus(1);
            pushFrequencyMapper.insert(pushFrequency);
        } else {
            pushFrequency.setUpdateTime(LocalDateTime.now());
            pushFrequencyMapper.updateById(pushFrequency);
        }
    }
    
    @Override
    @Transactional
    public void resetConfig() {
        // 将所有配置状态设为未启用
        PushFrequency config = new PushFrequency();
        config.setStatus(0);
        pushFrequencyMapper.update(config, new QueryWrapper<>());
    }
}


package com.hangyin.marketing.service.impl;

import com.hangyin.marketing.entity.MonitorIndicator;
import com.hangyin.marketing.mapper.MonitorIndicatorMapper;
import com.hangyin.marketing.service.MonitorIndicatorService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 监控指标服务实现类
 */
@Slf4j
@Service
public class MonitorIndicatorServiceImpl implements MonitorIndicatorService {
    
    @Autowired
    private MonitorIndicatorMapper monitorIndicatorMapper;
    
    @Override
    public MonitorIndicator getActiveConfig() {
        QueryWrapper<MonitorIndicator> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.orderByDesc("update_time");
        wrapper.last("LIMIT 1");
        return monitorIndicatorMapper.selectOne(wrapper);
    }
    
    @Override
    @Transactional
    public void saveOrUpdateConfig(MonitorIndicator indicator) {
        if (indicator.getId() == null) {
            indicator.setCreateTime(LocalDateTime.now());
            indicator.setUpdateTime(LocalDateTime.now());
            indicator.setStatus(1);
            monitorIndicatorMapper.insert(indicator);
        } else {
            indicator.setUpdateTime(LocalDateTime.now());
            monitorIndicatorMapper.updateById(indicator);
        }
    }
}


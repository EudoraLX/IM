package com.hangyin.marketing.service.impl;

import com.hangyin.marketing.common.PageRequest;
import com.hangyin.marketing.common.PageResult;
import com.hangyin.marketing.entity.ActivityRecord;
import com.hangyin.marketing.entity.Lead;
import com.hangyin.marketing.mapper.ActivityRecordMapper;
import com.hangyin.marketing.mapper.LeadMapper;
import com.hangyin.marketing.service.ActivityRecordService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活跃次数记录服务实现类
 */
@Slf4j
@Service
public class ActivityRecordServiceImpl implements ActivityRecordService {
    
    @Autowired
    private ActivityRecordMapper activityRecordMapper;
    
    @Autowired
    private LeadMapper leadMapper;
    
    @Override
    @Transactional
    public ActivityRecord recordActivity(ActivityRecord activityRecord) {
        // 根据线索编号查找线索ID
        if (activityRecord.getLeadNo() != null && activityRecord.getLeadId() == null) {
            QueryWrapper<Lead> wrapper = new QueryWrapper<>();
            wrapper.eq("lead_no", activityRecord.getLeadNo());
            wrapper.eq("deleted", 0);
            Lead lead = leadMapper.selectOne(wrapper);
            if (lead != null) {
                activityRecord.setLeadId(lead.getId());
                log.info("找到线索: leadNo={}, leadId={}", activityRecord.getLeadNo(), lead.getId());
            } else {
                log.warn("未找到线索编号为 {} 的线索，lead_id 将为空", activityRecord.getLeadNo());
            }
        }
        
        // 设置默认值
        if (activityRecord.getActivityType() == null || activityRecord.getActivityType().isEmpty()) {
            activityRecord.setActivityType("测试");
        }
        if (activityRecord.getActivityTime() == null) {
            activityRecord.setActivityTime(LocalDateTime.now());
        }
        activityRecord.setCreateTime(LocalDateTime.now());
        
        activityRecordMapper.insert(activityRecord);
        log.info("记录活跃次数: id={}, leadNo={}, leadId={}, activityType={}, organization={}, activityTime={}", 
                activityRecord.getId(), activityRecord.getLeadNo(), activityRecord.getLeadId(), 
                activityRecord.getActivityType(), activityRecord.getOrganization(), activityRecord.getActivityTime());
        
        return activityRecord;
    }
    
    @Override
    public PageResult<ActivityRecord> getActivityRecordsByLeadNo(String leadNo, PageRequest pageRequest) {
        Integer offset = pageRequest.getOffset();
        Integer size = pageRequest.getSize();
        
        List<ActivityRecord> records = activityRecordMapper.selectByLeadNo(leadNo, offset, size);
        Long total = activityRecordMapper.countByLeadNo(leadNo);
        
        return new PageResult<>(total, records, pageRequest.getCurrent(), pageRequest.getSize());
    }
    
    @Override
    public int getActivityCountByLeadNo(String leadNo) {
        // 使用自定义的 XML 查询方法，避免 MyBatis Plus 自动生成 SQL 的问题
        Long count = activityRecordMapper.countByLeadNo(leadNo);
        return count != null ? count.intValue() : 0;
    }
}


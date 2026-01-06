package com.hangyin.marketing.service.impl;

import com.hangyin.marketing.common.PageRequest;
import com.hangyin.marketing.common.PageResult;
import com.hangyin.marketing.entity.HighPotentialLead;
import com.hangyin.marketing.mapper.HighPotentialLeadMapper;
import com.hangyin.marketing.service.HighPotentialLeadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 高潜线索服务实现类
 */
@Slf4j
@Service
public class HighPotentialLeadServiceImpl implements HighPotentialLeadService {
    
    @Autowired
    private HighPotentialLeadMapper highPotentialLeadMapper;
    
    @Override
    public PageResult<HighPotentialLead> getHighPotentialLeadList(PageRequest pageRequest, String keyword, String status) {
        Integer offset = pageRequest.getOffset();
        Integer size = pageRequest.getSize();
        
        List<HighPotentialLead> records = highPotentialLeadMapper.selectHighPotentialLeadList(keyword, status, offset, size);
        Long total = highPotentialLeadMapper.countHighPotentialLeads(keyword, status);
        
        return new PageResult<>(total, records, pageRequest.getCurrent(), pageRequest.getSize());
    }
    
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 命中用户总数
        Long totalMatched = highPotentialLeadMapper.countHighPotentialLeads(null, null);
        stats.put("totalMatched", totalMatched);
        
        // 今日新增线索（简化处理，实际应该按日期统计）
        stats.put("todayNew", 8);
        
        // 待营销处理数量
        Long pendingCount = highPotentialLeadMapper.countHighPotentialLeads(null, "待营销处理");
        stats.put("pendingCount", pendingCount);
        
        // 营销转化成功率（简化处理）
        stats.put("conversionRate", 18.5);
        
        return stats;
    }
    
    @Override
    @Transactional
    public void batchCall(List<Long> ids) {
        // TODO: 对接甲方内部外呼系统
        log.info("批量外呼，线索IDs: {}", ids);
        // 更新状态为"营销进行中"
        for (Long id : ids) {
            HighPotentialLead lead = new HighPotentialLead();
            lead.setId(id);
            lead.setStatus("营销进行中");
            lead.setUpdateTime(LocalDateTime.now());
            highPotentialLeadMapper.updateById(lead);
        }
    }
    
    @Override
    @Transactional
    public void batchSendSms(List<Long> ids) {
        // TODO: 对接甲方内部短信系统
        log.info("批量发送短信，线索IDs: {}", ids);
        // 更新状态为"营销进行中"
        for (Long id : ids) {
            HighPotentialLead lead = new HighPotentialLead();
            lead.setId(id);
            lead.setStatus("营销进行中");
            lead.setUpdateTime(LocalDateTime.now());
            highPotentialLeadMapper.updateById(lead);
        }
    }
    
    @Override
    @Transactional
    public void updateLeadStatus(Long id, String status) {
        HighPotentialLead lead = new HighPotentialLead();
        lead.setId(id);
        lead.setStatus(status);
        lead.setUpdateTime(LocalDateTime.now());
        highPotentialLeadMapper.updateById(lead);
    }
    
    @Override
    @Transactional
    public void deleteHighPotentialLead(Long id) {
        highPotentialLeadMapper.deleteById(id);
    }
    
    @Override
    @Transactional
    public void batchDeleteHighPotentialLeads(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            highPotentialLeadMapper.deleteBatchIds(ids);
        }
    }
}


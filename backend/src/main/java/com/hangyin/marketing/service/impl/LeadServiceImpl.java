package com.hangyin.marketing.service.impl;

import com.hangyin.marketing.common.PageRequest;
import com.hangyin.marketing.common.PageResult;
import com.hangyin.marketing.entity.Lead;
import com.hangyin.marketing.mapper.LeadMapper;
import com.hangyin.marketing.service.LeadService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 线索服务实现类
 */
@Slf4j
@Service
public class LeadServiceImpl implements LeadService {
    
    @Autowired
    private LeadMapper leadMapper;
    
    @Override
    public PageResult<Lead> getLeadList(PageRequest pageRequest, String keyword, String status) {
        Integer offset = pageRequest.getOffset();
        Integer size = pageRequest.getSize();
        
        List<Lead> records = leadMapper.selectLeadList(keyword, status, offset, size);
        Long total = leadMapper.countLeads(keyword, status);
        
        return new PageResult<>(total, records, pageRequest.getCurrent(), pageRequest.getSize());
    }
    
    @Override
    public Lead getLeadById(Long id) {
        return leadMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public void addLead(Lead lead) {
        // 生成线索编号
        if (lead.getLeadNo() == null || lead.getLeadNo().isEmpty()) {
            lead.setLeadNo("L" + System.currentTimeMillis());
        }
        lead.setCreateTime(LocalDateTime.now());
        lead.setUpdateTime(LocalDateTime.now());
        lead.setDeleted(0);
        if (lead.getStatus() == null || lead.getStatus().isEmpty()) {
            lead.setStatus("新线索");
        }
        leadMapper.insert(lead);
    }
    
    @Override
    @Transactional
    public void updateLead(Lead lead) {
        lead.setUpdateTime(LocalDateTime.now());
        leadMapper.updateById(lead);
    }
    
    @Override
    @Transactional
    public void deleteLead(Long id) {
        // 使用物理删除（真正从数据库删除数据）
        // 注意：这会真正删除数据，无法恢复
        leadMapper.physicalDeleteById(id);
        
        // 如果使用逻辑删除（数据可恢复），使用下面的代码：
        // leadMapper.deleteById(id);
    }
    
    @Override
    @Transactional
    public void batchDeleteLeads(List<Long> ids) {
        for (Long id : ids) {
            deleteLead(id);
        }
    }
    
    @Override
    @Transactional
    public void batchImportLeads(List<Lead> leads) {
        for (Lead lead : leads) {
            if (lead.getLeadNo() == null || lead.getLeadNo().isEmpty()) {
                lead.setLeadNo("L" + System.currentTimeMillis());
            }
            lead.setCreateTime(LocalDateTime.now());
            lead.setUpdateTime(LocalDateTime.now());
            lead.setDeleted(0);
            if (lead.getStatus() == null || lead.getStatus().isEmpty()) {
                lead.setStatus("新线索");
            }
            leadMapper.insert(lead);
        }
    }
}


package com.hangyin.marketing.service;

import com.hangyin.marketing.common.PageRequest;
import com.hangyin.marketing.common.PageResult;
import com.hangyin.marketing.entity.HighPotentialLead;

import java.util.List;
import java.util.Map;

/**
 * 高潜线索服务接口
 */
public interface HighPotentialLeadService {
    
    /**
     * 分页查询高潜线索列表
     */
    PageResult<HighPotentialLead> getHighPotentialLeadList(PageRequest pageRequest, String keyword, String status);
    
    /**
     * 获取统计信息
     */
    Map<String, Object> getStatistics();
    
    /**
     * 批量外呼
     */
    void batchCall(List<Long> ids);
    
    /**
     * 批量发送短信
     */
    void batchSendSms(List<Long> ids);
    
    /**
     * 更新线索状态
     */
    void updateLeadStatus(Long id, String status);
    
    /**
     * 删除高潜线索
     */
    void deleteHighPotentialLead(Long id);
    
    /**
     * 批量删除高潜线索
     */
    void batchDeleteHighPotentialLeads(List<Long> ids);
}


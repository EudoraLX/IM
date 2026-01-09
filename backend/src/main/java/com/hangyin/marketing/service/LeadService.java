package com.hangyin.marketing.service;

import com.hangyin.marketing.common.PageRequest;
import com.hangyin.marketing.common.PageResult;
import com.hangyin.marketing.entity.Lead;

import java.util.List;

/**
 * 线索服务接口
 */
public interface LeadService {
    
    /**
     * 分页查询线索列表
     */
    PageResult<Lead> getLeadList(PageRequest pageRequest, String keyword, String status);
    
    /**
     * 根据ID查询线索
     */
    Lead getLeadById(Long id);
    
    /**
     * 根据线索编号查询线索
     */
    Lead getLeadByLeadNo(String leadNo);
    
    /**
     * 新增线索
     */
    void addLead(Lead lead);
    
    /**
     * 更新线索
     */
    void updateLead(Lead lead);
    
    /**
     * 删除线索
     */
    void deleteLead(Long id);
    
    /**
     * 批量删除线索
     */
    void batchDeleteLeads(List<Long> ids);
    
    /**
     * 批量导入线索
     */
    void batchImportLeads(List<Lead> leads);
}


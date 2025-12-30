package com.hangyin.marketing.controller;

import com.hangyin.marketing.common.PageRequest;
import com.hangyin.marketing.common.PageResult;
import com.hangyin.marketing.common.Result;
import com.hangyin.marketing.entity.Lead;
import com.hangyin.marketing.service.LeadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 线索管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/lead")
@CrossOrigin
public class LeadController {
    
    @Autowired
    private LeadService leadService;
    
    /**
     * 分页查询线索列表
     */
    @GetMapping("/list")
    public Result<PageResult<Lead>> getLeadList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setCurrent(current);
        pageRequest.setSize(size);
        PageResult<Lead> result = leadService.getLeadList(pageRequest, keyword, status);
        return Result.success(result);
    }
    
    /**
     * 根据ID查询线索
     */
    @GetMapping("/{id}")
    public Result<Lead> getLeadById(@PathVariable Long id) {
        Lead lead = leadService.getLeadById(id);
        return Result.success(lead);
    }
    
    /**
     * 新增线索
     */
    @PostMapping("/add")
    public Result<Void> addLead(@RequestBody Lead lead) {
        leadService.addLead(lead);
        return Result.success();
    }
    
    /**
     * 更新线索
     */
    @PutMapping("/update")
    public Result<Void> updateLead(@RequestBody Lead lead) {
        leadService.updateLead(lead);
        return Result.success();
    }
    
    /**
     * 删除线索
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteLead(@PathVariable Long id) {
        leadService.deleteLead(id);
        return Result.success();
    }
    
    /**
     * 批量删除线索
     */
    @PostMapping("/batchDelete")
    public Result<Void> batchDeleteLeads(@RequestBody List<Long> ids) {
        leadService.batchDeleteLeads(ids);
        return Result.success();
    }
    
    /**
     * 批量导入线索
     */
    @PostMapping("/batchImport")
    public Result<Void> batchImportLeads(@RequestBody List<Lead> leads) {
        leadService.batchImportLeads(leads);
        return Result.success();
    }
}


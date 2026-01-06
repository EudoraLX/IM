package com.hangyin.marketing.controller;

import com.hangyin.marketing.common.PageRequest;
import com.hangyin.marketing.common.PageResult;
import com.hangyin.marketing.common.Result;
import com.hangyin.marketing.entity.HighPotentialLead;
import com.hangyin.marketing.service.HighPotentialLeadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 高潜线索营销控制器
 */
@Slf4j
@RestController
@RequestMapping("/highPotential")
@CrossOrigin
public class HighPotentialLeadController {
    
    @Autowired
    private HighPotentialLeadService highPotentialLeadService;
    
    /**
     * 分页查询高潜线索列表
     */
    @GetMapping("/list")
    public Result<PageResult<HighPotentialLead>> getHighPotentialLeadList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setCurrent(current);
        pageRequest.setSize(size);
        PageResult<HighPotentialLead> result = highPotentialLeadService.getHighPotentialLeadList(pageRequest, keyword, status);
        return Result.success(result);
    }
    
    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = highPotentialLeadService.getStatistics();
        return Result.success(statistics);
    }
    
    /**
     * 批量外呼
     */
    @PostMapping("/batchCall")
    public Result<Void> batchCall(@RequestBody List<Long> ids) {
        highPotentialLeadService.batchCall(ids);
        return Result.success();
    }
    
    /**
     * 批量发送短信
     */
    @PostMapping("/batchSms")
    public Result<Void> batchSendSms(@RequestBody List<Long> ids) {
        highPotentialLeadService.batchSendSms(ids);
        return Result.success();
    }
    
    /**
     * 更新线索状态
     */
    @PutMapping("/status")
    public Result<Void> updateLeadStatus(@RequestParam Long id, @RequestParam String status) {
        highPotentialLeadService.updateLeadStatus(id, status);
        return Result.success();
    }
    
    /**
     * 删除高潜线索
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteHighPotentialLead(@PathVariable Long id) {
        highPotentialLeadService.deleteHighPotentialLead(id);
        return Result.success();
    }
    
    /**
     * 批量删除高潜线索
     */
    @PostMapping("/batchDelete")
    public Result<Void> batchDeleteHighPotentialLeads(@RequestBody List<Long> ids) {
        highPotentialLeadService.batchDeleteHighPotentialLeads(ids);
        return Result.success();
    }
}


package com.hangyin.marketing.controller;

import com.hangyin.marketing.common.PageRequest;
import com.hangyin.marketing.common.PageResult;
import com.hangyin.marketing.common.Result;
import com.hangyin.marketing.entity.ActivityRecord;
import com.hangyin.marketing.service.ActivityRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 活跃次数记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/activityRecord")
@CrossOrigin
public class ActivityRecordController {
    
    @Autowired
    private ActivityRecordService activityRecordService;
    
    /**
     * 记录活跃次数
     */
    @PostMapping("/record")
    public Result<ActivityRecord> recordActivity(@RequestBody ActivityRecord activityRecord) {
        try {
            if (activityRecord.getLeadNo() == null || activityRecord.getLeadNo().isEmpty()) {
                return Result.error("线索编号不能为空");
            }
            
            ActivityRecord record = activityRecordService.recordActivity(activityRecord);
            return Result.success("活跃次数记录成功", record);
        } catch (Exception e) {
            log.error("记录活跃次数失败", e);
            return Result.error("记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据线索编号查询活跃记录列表
     */
    @GetMapping("/list")
    public Result<PageResult<ActivityRecord>> getActivityRecords(
            @RequestParam String leadNo,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            PageRequest pageRequest = new PageRequest();
            pageRequest.setCurrent(current);
            pageRequest.setSize(size);
            PageResult<ActivityRecord> result = activityRecordService.getActivityRecordsByLeadNo(leadNo, pageRequest);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询活跃记录失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据线索编号获取活跃次数
     */
    @GetMapping("/count")
    public Result<Integer> getActivityCount(@RequestParam String leadNo) {
        try {
            int count = activityRecordService.getActivityCountByLeadNo(leadNo);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取活跃次数失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }
}


package com.hangyin.marketing.service;

import com.hangyin.marketing.common.PageRequest;
import com.hangyin.marketing.common.PageResult;
import com.hangyin.marketing.entity.ActivityRecord;

/**
 * 活跃次数记录服务接口
 */
public interface ActivityRecordService {
    
    /**
     * 记录活跃次数
     * 
     * @param activityRecord 活跃记录
     * @return 记录结果
     */
    ActivityRecord recordActivity(ActivityRecord activityRecord);
    
    /**
     * 根据线索编号查询活跃记录列表
     * 
     * @param leadNo 线索编号
     * @param pageRequest 分页请求
     * @return 分页结果
     */
    PageResult<ActivityRecord> getActivityRecordsByLeadNo(String leadNo, PageRequest pageRequest);
    
    /**
     * 根据线索编号获取活跃次数
     * 
     * @param leadNo 线索编号
     * @return 活跃次数
     */
    int getActivityCountByLeadNo(String leadNo);
}


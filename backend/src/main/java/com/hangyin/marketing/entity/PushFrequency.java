package com.hangyin.marketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 推送频率配置实体类
 */
@Data
@TableName("push_frequency")
public class PushFrequency {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 监控指标ID
     */
    private Long monitorIndicatorId;
    
    /**
     * 频率类型：hourly-每小时，daily-每日，weekly-每周，monthly-每月，custom-自定义
     */
    private String frequencyType;
    
    /**
     * 推送间隔（分钟）
     */
    private Integer pushInterval;
    
    /**
     * 频率策略配置（JSON格式，存储每周分布）
     */
    private String frequencyStrategy;
    
    /**
     * 周末规则：0-不启用，1-启用
     */
    private Integer weekendRule;
    
    /**
     * 最小推送阈值（秒）
     */
    private Integer minPushThreshold;
    
    /**
     * 配置状态：0-未启用，1-已启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}


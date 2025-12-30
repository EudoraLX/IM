package com.hangyin.marketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 监控指标配置实体类
 */
@Data
@TableName("monitor_indicator")
public class MonitorIndicator {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 监控时间范围（天数）
     */
    private Integer timeRangeDays;
    
    /**
     * 活跃机构（JSON格式存储多个机构）
     */
    private String activeOrganizations;
    
    /**
     * 活跃次数阈值
     */
    private Integer activityThreshold;
    
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


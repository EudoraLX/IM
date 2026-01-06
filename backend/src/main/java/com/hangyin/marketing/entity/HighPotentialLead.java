package com.hangyin.marketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 高潜线索实体类
 */
@Data
@TableName("high_potential_lead")
public class HighPotentialLead {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 线索ID
     */
    private Long leadId;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 命中规则
     */
    private String matchingRule;
    
    /**
     * 线索来源系统
     */
    private String leadSourceSystem;
    
    /**
     * 命中日期
     */
    private LocalDateTime matchingDate;
    
    /**
     * 状态：待营销处理、营销进行中、营销已转化、已联系失效
     */
    private String status;
    
    /**
     * 高潜原因
     */
    private String highPotentialReason;
    
    /**
     * 线索分级：A-高潜，B-中潜，C-低潜
     */
    private String leadLevel;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 客户姓名（从lead表关联查询，不映射到数据库）
     */
    @TableField(exist = false)
    private String customerName;
    
    /**
     * 联系电话（从lead表关联查询，不映射到数据库）
     */
    @TableField(exist = false)
    private String contactPhone;
}


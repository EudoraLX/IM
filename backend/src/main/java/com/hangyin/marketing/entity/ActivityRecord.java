package com.hangyin.marketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 活跃次数记录实体类
 */
@Data
@TableName("activity_record")
public class ActivityRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 线索编号
     */
    private String leadNo;
    
    /**
     * 线索ID（冗余字段，用于快速查询）
     */
    private Long leadId;
    
    /**
     * 活跃类型：测试、人脸验证、OCR验证、登录等
     */
    private String activityType;
    
    /**
     * 活跃机构
     */
    private String organization;
    
    /**
     * 活跃时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime activityTime;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}


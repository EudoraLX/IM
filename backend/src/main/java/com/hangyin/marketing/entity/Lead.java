package com.hangyin.marketing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 线索实体类
 */
@Data
@TableName("lead")
public class Lead {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 线索编号
     */
    private String leadNo;
    
    /**
     * 客户姓名
     */
    private String customerName;
    
    /**
     * 联系电话
     */
    private String contactPhone;
    
    /**
     * 身份证号（哈希值）
     */
    private String idCardHash;
    
    /**
     * 来源渠道
     */
    private String sourceChannel;
    
    /**
     * 状态：新线索、跟进中、已转化、已失效
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 是否删除：0-未删除，1-已删除
     */
    private Integer deleted;
}


package com.hangyin.marketing.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 活跃度数据服务接口
 * 用于对接第三方数据源（人脸验证、OCR验证等事件日志）
 */
public interface ActivityDataService {
    
    /**
     * 获取线索在指定时间范围内的活跃次数
     * 
     * @param leadId 线索ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param organizations 活跃机构列表（用于过滤）
     * @return 活跃次数
     */
    int getActivityCount(Long leadId, LocalDateTime startTime, LocalDateTime endTime, List<String> organizations);
    
    /**
     * 获取线索的活跃事件详情
     * 
     * @param leadId 线索ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 活跃事件列表
     */
    List<ActivityEvent> getActivityEvents(Long leadId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 批量获取多个线索的活跃次数
     * 
     * @param leadIds 线索ID列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param organizations 活跃机构列表
     * @return Map<线索ID, 活跃次数>
     */
    Map<Long, Integer> batchGetActivityCounts(List<Long> leadIds, LocalDateTime startTime, 
                                              LocalDateTime endTime, List<String> organizations);
    
    /**
     * 活跃事件类型枚举
     */
    enum EventType {
        FACE_VERIFICATION,  // 人脸验证
        ID_CARD_OCR,        // 身份证OCR验证
        DATA_CALL,          // 数据调用
        LOGIN,              // 登录
        OTHER               // 其他
    }
    
    /**
     * 活跃事件实体
     */
    class ActivityEvent {
        private Long leadId;
        private EventType eventType;
        private String organization;
        private LocalDateTime eventTime;
        private String eventDetail;
        
        // Getters and Setters
        public Long getLeadId() { return leadId; }
        public void setLeadId(Long leadId) { this.leadId = leadId; }
        
        public EventType getEventType() { return eventType; }
        public void setEventType(EventType eventType) { this.eventType = eventType; }
        
        public String getOrganization() { return organization; }
        public void setOrganization(String organization) { this.organization = organization; }
        
        public LocalDateTime getEventTime() { return eventTime; }
        public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
        
        public String getEventDetail() { return eventDetail; }
        public void setEventDetail(String eventDetail) { this.eventDetail = eventDetail; }
    }
}


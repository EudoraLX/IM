package com.hangyin.marketing.service.impl;

import com.hangyin.marketing.mapper.ActivityRecordMapper;
import com.hangyin.marketing.service.ActivityDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 活跃度数据服务实现类
 * 对接第三方数据源API（人脸验证、OCR验证等事件日志）
 */
@Slf4j
@Service
public class ActivityDataServiceImpl implements ActivityDataService {
    
    @Autowired(required = false)
    private RestTemplate restTemplate;
    
    @Autowired
    private ActivityRecordMapper activityRecordMapper;
    
    /**
     * 第三方数据源API地址
     * 可在 application.yml 中配置
     */
    @Value("${activity.data.api.url:http://localhost:8081/api/activity}")
    private String activityDataApiUrl;
    
    /**
     * 是否启用第三方数据源
     * 如果为false，优先使用数据库记录，再使用模拟数据
     */
    @Value("${activity.data.enabled:false}")
    private boolean activityDataEnabled;
    
    /**
     * 是否优先使用数据库记录
     * 如果为true，优先从数据库读取活跃次数记录
     */
    @Value("${activity.data.use-database:true}")
    private boolean useDatabase;
    
    @Override
    public int getActivityCount(Long leadId, LocalDateTime startTime, LocalDateTime endTime, List<String> organizations) {
        // 优先从数据库读取活跃次数记录
        if (useDatabase) {
            try {
                // 如果 organizations 为空，传入 null，不过滤机构
                List<String> orgFilter = (organizations != null && !organizations.isEmpty()) ? organizations : null;
                int count = activityRecordMapper.countByLeadIdAndTimeRange(leadId, startTime, endTime, orgFilter);
                log.info("从数据库获取活跃次数: leadId={}, startTime={}, endTime={}, count={}, organizations={}, useDatabase={}", 
                        leadId, startTime, endTime, count, organizations, useDatabase);
                return count;
            } catch (Exception e) {
                log.error("从数据库获取活跃次数失败，leadId: {}, startTime: {}, endTime: {}, organizations: {}", 
                        leadId, startTime, endTime, organizations, e);
                // 如果数据库查询失败，继续尝试其他方式
            }
        }
        
        if (!activityDataEnabled) {
            // 如果未启用第三方数据源，返回模拟数据
            log.debug("第三方数据源未启用，使用模拟数据，leadId: {}", leadId);
            return getMockActivityCount(leadId, startTime, endTime, organizations);
        }
        
        try {
            if (restTemplate == null) {
                restTemplate = new RestTemplate();
            }
            
            // ========== 在这里修改代码，对接客户端APP的API ==========
            // 方式1：GET请求（推荐）
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(activityDataApiUrl + "/count")
                .queryParam("leadId", leadId)
                .queryParam("startTime", startTime.toString())
                .queryParam("endTime", endTime.toString());
            
            if (organizations != null && !organizations.isEmpty()) {
                builder.queryParam("organizations", String.join(",", organizations));
            }
            
            String url = builder.toUriString();
            log.debug("调用活跃度API: {}", url);
            
            // ========== 在这里修改代码，对接客户端APP的API ==========
            // 方式1：如果API返回JSON对象，例如：{"activityCount": 5}
            try {
                @SuppressWarnings("unchecked")
                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> body = (Map<String, Object>) response.getBody();
                    Object countObj = body.get("activityCount");
                    if (countObj != null) {
                        int count = Integer.parseInt(countObj.toString());
                        log.debug("获取到活跃次数: leadId={}, count={}", leadId, count);
                        return count;
                    }
                }
            } catch (Exception e) {
                log.warn("解析JSON响应失败，尝试直接获取数字", e);
            }
            
            // 方式2：如果API直接返回数字
            try {
                Integer count = restTemplate.getForObject(url, Integer.class);
                if (count != null) {
                    log.debug("获取到活跃次数: leadId={}, count={}", leadId, count);
                    return count;
                }
            } catch (Exception e) {
                log.warn("直接获取数字失败", e);
            }
            
            // ========== 如果API未实现，使用模拟数据 ==========
            log.warn("第三方数据源API未实现或返回异常，使用模拟数据，leadId: {}", leadId);
            return getMockActivityCount(leadId, startTime, endTime, organizations);
            
        } catch (Exception e) {
            log.error("获取活跃次数失败，leadId: {}，使用模拟数据", leadId, e);
            // 失败时返回模拟数据，确保系统可用
            return getMockActivityCount(leadId, startTime, endTime, organizations);
        }
    }
    
    @Override
    public List<ActivityEvent> getActivityEvents(Long leadId, LocalDateTime startTime, LocalDateTime endTime) {
        if (!activityDataEnabled) {
            return getMockActivityEvents(leadId, startTime, endTime);
        }
        
        try {
            // 调用第三方API获取活跃事件详情
            // TODO: 实现实际的API调用
            log.warn("获取活跃事件详情API未实现，使用模拟数据");
            return getMockActivityEvents(leadId, startTime, endTime);
        } catch (Exception e) {
            log.error("获取活跃事件失败，leadId: {}", leadId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Map<Long, Integer> batchGetActivityCounts(List<Long> leadIds, LocalDateTime startTime, 
                                                     LocalDateTime endTime, List<String> organizations) {
        Map<Long, Integer> result = new HashMap<>();
        
        if (!activityDataEnabled) {
            // 使用模拟数据
            for (Long leadId : leadIds) {
                result.put(leadId, getMockActivityCount(leadId, startTime, endTime, organizations));
            }
            return result;
        }
        
        try {
            // 批量调用第三方API
            // TODO: 实现实际的批量API调用
            log.warn("批量获取活跃次数API未实现，使用模拟数据");
            for (Long leadId : leadIds) {
                result.put(leadId, getMockActivityCount(leadId, startTime, endTime, organizations));
            }
            return result;
        } catch (Exception e) {
            log.error("批量获取活跃次数失败", e);
            // 失败时返回模拟数据
            for (Long leadId : leadIds) {
                result.put(leadId, 0);
            }
            return result;
        }
    }
    
    /**
     * 获取模拟活跃次数（用于测试和开发）
     * 
     * 模拟规则：
     * 1. 线索ID为偶数的线索，活跃次数较高（模拟高潜线索）
     * 2. 线索ID为奇数的线索，活跃次数较低（模拟普通线索）
     * 3. 可以根据线索的其他属性（如来源渠道）调整活跃次数
     */
    private int getMockActivityCount(Long leadId, LocalDateTime startTime, LocalDateTime endTime, List<String> organizations) {
        // 基础活跃次数：根据线索ID生成（0-5之间）
        int baseCount = (int) (leadId % 6);
        
        // 规则1：线索ID为偶数的线索，活跃次数较高（+5），模拟高潜线索
        if (leadId % 2 == 0) {
            baseCount += 5;  // 活跃次数在 5-10 之间
        }
        
        // 规则2：如果线索ID能被3整除，再增加活跃次数（模拟超高活跃）
        if (leadId % 3 == 0) {
            baseCount += 2;
        }
        
        // 规则3：如果线索ID能被5整除，再增加活跃次数
        if (leadId % 5 == 0) {
            baseCount += 1;
        }
        
        // 确保活跃次数在合理范围内（0-15）
        return Math.min(baseCount, 15);
    }
    
    /**
     * 获取模拟活跃事件（用于测试和开发）
     */
    private List<ActivityEvent> getMockActivityEvents(Long leadId, LocalDateTime startTime, LocalDateTime endTime) {
        List<ActivityEvent> events = new ArrayList<>();
        
        // 模拟生成一些活跃事件
        int count = getMockActivityCount(leadId, startTime, endTime, null);
        for (int i = 0; i < count && i < 5; i++) {
            ActivityEvent event = new ActivityEvent();
            event.setLeadId(leadId);
            event.setEventType(ActivityDataService.EventType.FACE_VERIFICATION);
            event.setOrganization("华东中心机构");
            event.setEventTime(startTime.plusHours(i * 2));
            event.setEventDetail("人脸验证事件");
            events.add(event);
        }
        
        return events;
    }
}


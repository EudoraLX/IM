package com.hangyin.marketing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hangyin.marketing.entity.ActivityRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 活跃次数记录Mapper
 */
@Mapper
public interface ActivityRecordMapper extends BaseMapper<ActivityRecord> {
    
    /**
     * 根据线索编号和时间范围统计活跃次数
     */
    int countByLeadNoAndTimeRange(@Param("leadNo") String leadNo,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime,
                                  @Param("organizations") List<String> organizations);
    
    /**
     * 根据线索ID和时间范围统计活跃次数
     */
    int countByLeadIdAndTimeRange(@Param("leadId") Long leadId,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime,
                                  @Param("organizations") List<String> organizations);
    
    /**
     * 根据线索编号查询活跃记录列表
     */
    List<ActivityRecord> selectByLeadNo(@Param("leadNo") String leadNo,
                                        @Param("offset") Integer offset,
                                        @Param("size") Integer size);
    
    /**
     * 根据线索编号统计活跃记录总数
     */
    Long countByLeadNo(@Param("leadNo") String leadNo);
}


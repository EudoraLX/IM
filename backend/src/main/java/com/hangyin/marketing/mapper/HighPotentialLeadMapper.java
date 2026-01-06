package com.hangyin.marketing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hangyin.marketing.entity.HighPotentialLead;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 高潜线索Mapper
 */
@Mapper
public interface HighPotentialLeadMapper extends BaseMapper<HighPotentialLead> {
    
    /**
     * 根据条件查询高潜线索列表
     */
    List<HighPotentialLead> selectHighPotentialLeadList(@Param("keyword") String keyword,
                                                         @Param("status") String status,
                                                         @Param("offset") Integer offset,
                                                         @Param("size") Integer size);
    
    /**
     * 统计高潜线索数量
     */
    Long countHighPotentialLeads(@Param("keyword") String keyword,
                                  @Param("status") String status);
    
    /**
     * 统计今日新增的高潜线索数量
     */
    Long countTodayNewLeads();
    
    /**
     * 统计昨日结束时的总记录数（用于计算较昨日变化）
     */
    Long countYesterdayTotalLeads();
}


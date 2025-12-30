package com.hangyin.marketing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hangyin.marketing.entity.Lead;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 线索Mapper
 */
@Mapper
public interface LeadMapper extends BaseMapper<Lead> {
    
    /**
     * 根据条件查询线索列表
     */
    List<Lead> selectLeadList(@Param("keyword") String keyword, 
                              @Param("status") String status,
                              @Param("offset") Integer offset,
                              @Param("size") Integer size);
    
    /**
     * 统计符合条件的线索数量
     */
    Long countLeads(@Param("keyword") String keyword, 
                    @Param("status") String status);
}


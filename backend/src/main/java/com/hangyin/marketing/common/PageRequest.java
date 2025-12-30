package com.hangyin.marketing.common;

import lombok.Data;

/**
 * 分页请求参数
 */
@Data
public class PageRequest {
    private Integer current = 1;
    private Integer size = 10;

    public Integer getOffset() {
        return (current - 1) * size;
    }
}


package com.hangyin.marketing.common;

import lombok.Data;
import java.util.List;

/**
 * 分页结果封装
 */
@Data
public class PageResult<T> {
    private Long total;
    private List<T> records;
    private Integer current;
    private Integer size;

    public PageResult() {
    }

    public PageResult(Long total, List<T> records, Integer current, Integer size) {
        this.total = total;
        this.records = records;
        this.current = current;
        this.size = size;
    }
}


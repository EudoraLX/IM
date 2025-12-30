package com.hangyin.marketing.config;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 保留关键字表名处理器
 * 自动为保留关键字表名添加反引号
 */
@Component
public class ReservedKeywordTableNameHandler {

    /**
     * MySQL 保留关键字列表（部分）
     */
    private static final Set<String> RESERVED_KEYWORDS = new HashSet<>(Arrays.asList(
        "lead", "order", "group", "select", "table", "user", "key", "index"
    ));

    /**
     * 处理表名，如果是保留关键字则添加反引号
     */
    public String handleTableName(String tableName) {
        if (StringUtils.isBlank(tableName)) {
            return tableName;
        }
        
        // 如果已经包含反引号，直接返回
        if (tableName.startsWith("`") && tableName.endsWith("`")) {
            return tableName;
        }
        
        // 移除已有的反引号（如果有）
        String cleanName = tableName.replace("`", "");
        
        // 检查是否为保留关键字（不区分大小写）
        if (RESERVED_KEYWORDS.contains(cleanName.toLowerCase())) {
            return "`" + cleanName + "`";
        }
        
        return tableName;
    }
}


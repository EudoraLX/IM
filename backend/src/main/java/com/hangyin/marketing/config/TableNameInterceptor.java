package com.hangyin.marketing.config;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.Properties;

/**
 * MyBatis SQL 拦截器
 * 自动为保留关键字表名添加反引号
 * 拦截 StatementHandler.prepare 方法，在 SQL 准备阶段修改
 */
@Component
@Intercepts({
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class TableNameInterceptor implements Interceptor {

    /**
     * MySQL 保留关键字列表
     */
    private static final String[] RESERVED_KEYWORDS = {"lead", "order", "group", "select", "table", "user"};

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        
        // 获取 BoundSql（这是实际执行的 SQL）
        BoundSql boundSql = (BoundSql) metaObject.getValue("boundSql");
        String sql = boundSql.getSql();
        
        // 替换保留关键字表名
        String newSql = replaceReservedKeywordTableNames(sql);
        
        if (!sql.equals(newSql)) {
            // 使用 MetaObject 修改 BoundSql 中的 SQL
            metaObject.setValue("boundSql.sql", newSql);
            System.out.println("✓ SQL 拦截器已替换表名:");
            System.out.println("  原始: " + sql.substring(0, Math.min(150, sql.length())));
            System.out.println("  替换后: " + newSql.substring(0, Math.min(150, newSql.length())));
        }
        
        return invocation.proceed();
    }

    /**
     * 替换 SQL 中的保留关键字表名
     */
    private String replaceReservedKeywordTableNames(String sql) {
        String result = sql;
        
        // 使用正则表达式匹配表名并替换
        for (String keyword : RESERVED_KEYWORDS) {
            // 检查是否已经包含反引号，避免重复替换
            if (result.contains("`" + keyword + "`")) {
                continue;
            }
            
            // 匹配以下模式（支持多个空格）：
            // FROM lead、INTO lead、JOIN lead、UPDATE lead、TABLE lead
            // \s+ 匹配一个或多个空格
            // \b 单词边界确保精确匹配
            // (?i) 表示不区分大小写
            String[] patterns = {
                "(?i)\\bFROM\\s+" + keyword + "\\b",           // FROM lead
                "(?i)\\bINTO\\s+" + keyword + "\\b",          // INTO lead (支持多个空格)
                "(?i)\\bJOIN\\s+" + keyword + "\\b",           // JOIN lead
                "(?i)\\bUPDATE\\s+" + keyword + "\\b",         // UPDATE lead
                "(?i)\\bTABLE\\s+" + keyword + "\\b",          // TABLE lead
                "(?i)\\bDELETE\\s+FROM\\s+" + keyword + "\\b"  // DELETE FROM lead
            };
            
            String[] replacements = {
                "FROM `" + keyword + "`",
                "INTO `" + keyword + "`",
                "JOIN `" + keyword + "`",
                "UPDATE `" + keyword + "`",
                "TABLE `" + keyword + "`",
                "DELETE FROM `" + keyword + "`"
            };
            
            for (int i = 0; i < patterns.length; i++) {
                String before = result;
                result = result.replaceAll(patterns[i], replacements[i]);
                if (!before.equals(result)) {
                    System.out.println("  匹配模式: " + patterns[i] + " -> " + replacements[i]);
                }
            }
        }
        
        return result;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以在这里设置属性
    }
}


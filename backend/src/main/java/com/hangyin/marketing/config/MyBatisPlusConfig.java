package com.hangyin.marketing.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Collection;

/**
 * MyBatis Plus 配置类
 * 解决表名为保留关键字的问题（自动为 lead 表名添加反引号）
 */
@Configuration
public class MyBatisPlusConfig implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired(required = false)
    private TableNameInterceptor tableNameInterceptor;

    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 在应用上下文完全刷新后注册拦截器
     * 使用 ContextRefreshedEvent 确保在 SqlSessionFactory 初始化完成后再注册
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 注册拦截器到所有 SqlSessionFactory
        if (tableNameInterceptor != null) {
            try {
                Collection<SqlSessionFactory> sqlSessionFactories = event.getApplicationContext()
                    .getBeansOfType(SqlSessionFactory.class).values();
                for (SqlSessionFactory sqlSessionFactory : sqlSessionFactories) {
                    org.apache.ibatis.session.Configuration configuration = sqlSessionFactory.getConfiguration();
                    if (!configuration.getInterceptors().contains(tableNameInterceptor)) {
                        configuration.addInterceptor(tableNameInterceptor);
                        System.out.println("✓ 已注册表名拦截器到 SqlSessionFactory");
                    }
                }
            } catch (Exception e) {
                System.err.println("✗ 注册表名拦截器失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}

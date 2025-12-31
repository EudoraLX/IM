package com.hangyin.marketing;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 智能营销管理平台启动类
 */
@SpringBootApplication
@MapperScan("com.hangyin.marketing.mapper")
@EnableScheduling  // 启用定时任务
public class MarketingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketingApplication.class, args);
    }
}


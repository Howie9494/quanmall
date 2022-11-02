package com.howie.quanmall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableDiscoveryClient
@EnableRedisHttpSession
@EnableFeignClients(basePackages = "com.howie.quanmall.search.feign")
public class QuanmallSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuanmallSearchApplication.class, args);
    }

}

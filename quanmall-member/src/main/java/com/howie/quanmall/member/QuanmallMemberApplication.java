package com.howie.quanmall.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@MapperScan("com.howie.quanmall.member.dao")
@SpringBootApplication
@EnableDiscoveryClient
@EnableRedisHttpSession
@EnableFeignClients(basePackages = "com.howie.quanmall.member.feign")
public class QuanmallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuanmallMemberApplication.class, args);
    }

}

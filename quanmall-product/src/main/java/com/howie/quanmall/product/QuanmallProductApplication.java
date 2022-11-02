package com.howie.quanmall.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@EnableRedisHttpSession
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.howie.quanmall.product.feign")
public class QuanmallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuanmallProductApplication.class, args);
    }

}

package com.howie.quanmall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@MapperScan("com.howie.quanmall.order.dao")
@EnableRabbit
@EnableRedisHttpSession
@EnableTransactionManagement
@EnableFeignClients
@SpringBootApplication
@EnableDiscoveryClient
public class QuanmallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuanmallOrderApplication.class, args);
    }

}

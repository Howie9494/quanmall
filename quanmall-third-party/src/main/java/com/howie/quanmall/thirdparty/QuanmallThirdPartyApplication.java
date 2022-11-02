package com.howie.quanmall.thirdparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class QuanmallThirdPartyApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuanmallThirdPartyApplication.class, args);
    }

}

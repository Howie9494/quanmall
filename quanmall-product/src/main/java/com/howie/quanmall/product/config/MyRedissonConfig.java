package com.howie.quanmall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author Howie
 * @Date 2022/1/27 18:08
 * @Version 1.0
 */
@Configuration
public class MyRedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson(){
        //创建配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.94.80:6379");
        //根据Config创建RedissonClient
        return Redisson.create(config);
    }
}

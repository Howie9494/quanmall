package com.howie.quanmall.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author Howie
 * @Date 2022/2/3 17:37
 * @Version 1.0
 */
@ConfigurationProperties(prefix = "quanmall.thread")
@Component
@Data
public class ThreadPoolConfigProperties {
    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;
}

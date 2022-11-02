package com.howie.quanmall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author Howie
 * @Date 2022/2/10 20:59
 * @Version 1.0
 */

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor(){
            @Override
            public void apply(RequestTemplate requestTemplate) {
                //获取刚进来的请求数据
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (requestAttributes!=null){
                    HttpServletRequest request = requestAttributes.getRequest();
                    if (request!=null){
                        //同步请求头数据
                        requestTemplate.header("Cookie",request.getHeader("Cookie"));
                    }
                }
            }
        };
    }
}

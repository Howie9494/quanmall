package com.howie.quanmall.order.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.PostConstruct;
import java.util.HashMap;

/**
 * @Author Howie
 * @Date 2022/2/10 14:45
 * @Version 1.0
 */

@Configuration
public class MyRabbitConfig {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @PostConstruct  //对象创建完成后，执行这个方法
    public void initRabbitTemplate(){
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean b, String s) {

            }
        });

        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int i, String s, String s1, String s2) {

            }
        });
    }

    @Bean
    public Queue orderDelayQueue(){
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","order-event-exchange");
        arguments.put("x-dead-letter-routing-key","order.release.order");
        arguments.put("x-message-ttl",1800000);
        return new Queue("order.delay.queue",true,false,false,arguments);
    }

    @Bean
    public Queue orderReleaseQueue(){
        return new Queue("order.release.queue",true,false,false);
    }

    @Bean
    public Exchange orderEventExchange(){
        return new TopicExchange("order-event-exchange",true,false);
    }

    @Bean
    public Binding orderCreateBinding(){
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.create.order",null);
    }

    @Bean
    public Binding orderReleaseBinding(){
        return new Binding("order.release.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.release.order",null);
    }

    /**
     * 订单释放绑定库存释放
     *  Created by Howie on 2022/2/12.
     */
    @Bean
    public Binding orderReleaseOtherBinding(){
        return new Binding("stock.release.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.release.other.#",null);
    }
}

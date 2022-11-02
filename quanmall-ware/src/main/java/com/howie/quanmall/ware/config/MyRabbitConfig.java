package com.howie.quanmall.ware.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public Exchange stockEventExchange(){
        return new TopicExchange("stock-event-exchange",true,false);
    }

    @Bean
    public Queue stockReleaseQueue(){
        return new Queue("stock.release.queue",true,false,false);
    }

    @Bean
    public Queue stockDelayQueue(){
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","stock-event-exchange");
        arguments.put("x-dead-letter-routing-key","stock.release");
        arguments.put("x-message-ttl",3600000);
        return new Queue("stock.delay.queue",true,false,false,arguments);
    }

    @Bean
    public Binding stockReleaseBinding(){
        return new Binding("stock.release.queue", Binding.DestinationType.QUEUE,"stock-event-exchange","stock.release.#",null);
    }

    @Bean
    public Binding stockDelayBinding(){
        return new Binding("stock.delay.queue", Binding.DestinationType.QUEUE,"stock-event-exchange","stock.locked",null);
    }
}

package com.howie.quanmall.order;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class QuanmallOrderApplicationTests {
    @Autowired
    AmqpAdmin amqpAdmin;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    public void createExchange(){
        amqpAdmin.declareExchange(new DirectExchange("hello-java-exchange",true,false));
        log.info("交换机创建完成");
    }

    @Test
    public void createQueue(){
        amqpAdmin.declareQueue(new Queue("hello-java-queue",true,false,false,null));
    }

    @Test
    public void createBinding(){
        amqpAdmin.declareBinding(new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello.java",
                null));
    }

    @Test
    public void sendMessage(){
        rabbitTemplate.convertAndSend("hello-java-exchange","hello.java","hello world");
    }
}

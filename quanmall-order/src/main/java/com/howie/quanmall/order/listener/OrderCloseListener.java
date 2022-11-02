package com.howie.quanmall.order.listener;

import com.howie.quanmall.order.config.AlipayTemplate;
import com.howie.quanmall.order.entity.OrderEntity;
import com.howie.quanmall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @Author Howie
 * @Date 2022/2/12 16:19
 * @Version 1.0
 */

@Service
@RabbitListener(queues = {"order.release.queue"})
public class OrderCloseListener {
    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void closeListener(OrderEntity entity, Message message, Channel channel) throws IOException {
        System.out.println("收到过期订单信息，准备关闭订单"+entity.getOrderSn());

        try {
            orderService.closeOrder(entity);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }
}

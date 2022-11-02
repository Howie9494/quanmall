package com.howie.quanmall.ware.listener;

import com.alibaba.fastjson.TypeReference;
import com.howie.common.to.mq.OrderTo;
import com.howie.common.to.mq.StockDetailTo;
import com.howie.common.to.mq.StockLockedTo;
import com.howie.common.utils.R;
import com.howie.quanmall.ware.entity.WareOrderTaskDetailEntity;
import com.howie.quanmall.ware.entity.WareOrderTaskEntity;
import com.howie.quanmall.ware.service.WareSkuService;
import com.howie.quanmall.ware.vo.OrderVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @Author Howie
 * @Date 2022/2/12 15:23
 * @Version 1.0
 */
@Service
@RabbitListener(queues = {"stock.release.queue"})
public class StockReleaseListener {
    @Autowired
    WareSkuService wareSkuService;

    /**
     * 监听死信，解锁库存
     *  Created by Howie on 2022/2/12.
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        System.out.println("收到解锁库存的信息");

        try{
            wareSkuService.unlockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }


    /**
     * 监听取消订单消息
     *  Created by Howie on 2022/2/12.
     */
    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo orderTo, Message message, Channel channel) throws IOException {
        System.out.println("订单关闭，准备解锁库存...");
        try{
            wareSkuService.unlockStock(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }

}

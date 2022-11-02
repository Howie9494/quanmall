package com.howie.quanmall.order.vo;

import com.howie.quanmall.order.entity.OrderEntity;
import com.howie.quanmall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author Howie
 * @Date 2022/2/11 15:23
 * @Version 1.0
 */
@Data
public class OrderCreateTo {
    private OrderEntity order;
    private List<OrderItemEntity> orderItems;
    private BigDecimal payPrice;
    private BigDecimal fare;
}

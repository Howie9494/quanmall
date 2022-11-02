package com.howie.quanmall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单提交数据
 * @Author Howie
 * @Date 2022/2/11 14:53
 * @Version 1.0
 */

@Data
public class OrderSubmitVo {
    private Long addrId;
    //无需提交需要购买的商品，去购物车重新获取
    private String orderToken;
    private BigDecimal payPrice;
    //用户相关信息可以去session中取
}

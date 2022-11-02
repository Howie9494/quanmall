package com.howie.quanmall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Author Howie
 * @Date 2022/2/10 19:58
 * @Version 1.0
 */

public class OrderConfirmVo {
    //收货地址
    @Setter @Getter
    List<MemberAddressVo> address;

    //所有选中的购物项
    @Setter @Getter
    List<OrderItemVo> items;

    //发票信息
    //优惠券信息
    @Setter @Getter
    Integer integration;

    @Setter @Getter
    Map<Long,Boolean> stocks;

    public Integer getCount(){
        Integer i = 0;
        if (items!=null){
            for (OrderItemVo item : items) {
                if (stocks.get(item.getSkuId())){
                    i += item.getCount();
                }
            }
        }
        return i;
    }

    //应付价格
    public BigDecimal getPayPrice() {
        return getTotal();
    }

    //订单总额
    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (items!=null){
            for (OrderItemVo item : items) {
                if (stocks.get(item.getSkuId())){
                    BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                    sum = sum.add(multiply);
                }
            }
        }
        return sum;
    }

    //防重领牌
    @Setter @Getter
    String orderToken;
}

package com.howie.quanmall.order.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author Howie
 * @Date 2022/2/11 16:32
 * @Version 1.0
 */

@Data
public class WareSkuLockVo {

    private String orderSn;
    private List<OrderItemVo> locks;
}

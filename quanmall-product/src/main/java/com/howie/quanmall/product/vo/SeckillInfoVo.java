package com.howie.quanmall.product.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author Howie
 * @Date 2022/2/13 19:44
 * @Version 1.0
 */
@Data
public class SeckillInfoVo {
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private BigDecimal seckillCount;
    /**
     * 每人限购数量
     */
    private BigDecimal seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;

    private Long startTime;

    private Long endTime;

    private String randomCode;
}

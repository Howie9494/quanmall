package com.howie.quanmall.seckill.service;

import com.howie.quanmall.seckill.to.SeckillSkuRedisTo;

import java.util.List;

/**
 * @Author Howie
 * @Date 2022/2/13 14:42
 * @Version 1.0
 */

public interface SeckillService {
    void uploadSeckillSkuLatest3Days();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    SeckillSkuRedisTo getSkuSeckillInfo(Long skuId);
}

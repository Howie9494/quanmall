package com.howie.quanmall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.howie.common.utils.PageUtils;
import com.howie.quanmall.coupon.entity.SeckillSkuRelationEntity;

import java.util.Map;

/**
 * 秒杀活动商品关联
 *
 * @author howie
 * @email HowieWu94@foxmail.com
 * @date 2022-01-18 19:16:07
 */
public interface SeckillSkuRelationService extends IService<SeckillSkuRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


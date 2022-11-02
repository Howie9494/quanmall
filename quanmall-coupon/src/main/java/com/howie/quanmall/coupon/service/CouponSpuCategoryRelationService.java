package com.howie.quanmall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.howie.common.utils.PageUtils;
import com.howie.quanmall.coupon.entity.CouponSpuCategoryRelationEntity;

import java.util.Map;

/**
 * 优惠券分类关联
 *
 * @author howie
 * @email HowieWu94@foxmail.com
 * @date 2022-01-18 19:16:07
 */
public interface CouponSpuCategoryRelationService extends IService<CouponSpuCategoryRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


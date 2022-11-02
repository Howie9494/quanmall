package com.howie.quanmall.coupon.dao;

import com.howie.quanmall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author howie
 * @email HowieWu94@foxmail.com
 * @date 2022-01-18 19:16:07
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}

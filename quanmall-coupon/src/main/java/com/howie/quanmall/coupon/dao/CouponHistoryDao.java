package com.howie.quanmall.coupon.dao;

import com.howie.quanmall.coupon.entity.CouponHistoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券领取历史记录
 * 
 * @author howie
 * @email HowieWu94@foxmail.com
 * @date 2022-01-18 19:16:07
 */
@Mapper
public interface CouponHistoryDao extends BaseMapper<CouponHistoryEntity> {
	
}

package com.howie.quanmall.coupon.dao;

import com.howie.quanmall.coupon.entity.SeckillSessionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀活动场次
 * 
 * @author howie
 * @email HowieWu94@foxmail.com
 * @date 2022-01-18 19:16:06
 */
@Mapper
public interface SeckillSessionDao extends BaseMapper<SeckillSessionEntity> {
	
}

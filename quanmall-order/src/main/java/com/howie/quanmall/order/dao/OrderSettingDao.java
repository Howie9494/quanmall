package com.howie.quanmall.order.dao;

import com.howie.quanmall.order.entity.OrderSettingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单配置信息
 * 
 * @author howie
 * @email HowieWu94@foxmail.com
 * @date 2022-01-18 19:45:14
 */
@Mapper
public interface OrderSettingDao extends BaseMapper<OrderSettingEntity> {
	
}

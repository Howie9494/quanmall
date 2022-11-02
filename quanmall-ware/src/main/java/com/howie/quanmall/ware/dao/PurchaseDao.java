package com.howie.quanmall.ware.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.howie.quanmall.ware.entity.PurchaseEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购信息
 * 
 * @author howie
 * @email HowieWu94@foxmail.com
 * @date 2022-01-24 21:23:09
 */
@Mapper
public interface PurchaseDao extends BaseMapper<PurchaseEntity> {
	
}

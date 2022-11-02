package com.howie.quanmall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.howie.common.to.SkuHasStockVo;
import com.howie.common.to.mq.OrderTo;
import com.howie.common.to.mq.StockLockedTo;
import com.howie.common.utils.PageUtils;
import com.howie.quanmall.ware.entity.WareSkuEntity;
import com.howie.quanmall.ware.vo.LockStockResult;
import com.howie.quanmall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author howie
 * @email HowieWu94@foxmail.com
 * @date 2022-01-18 19:51:49
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVo vo);

    void unlockStock(StockLockedTo to);

    void unlockStock(OrderTo orderTo);
}


package com.howie.quanmall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.howie.common.utils.PageUtils;
import com.howie.quanmall.ware.entity.PurchaseDetailEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author howie
 * @email HowieWu94@foxmail.com
 * @date 2022-01-24 21:23:09
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<PurchaseDetailEntity> listDetailByPurchaseId(Long id);
}


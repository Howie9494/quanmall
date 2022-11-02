package com.howie.quanmall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.howie.common.utils.PageUtils;
import com.howie.quanmall.ware.entity.WareOrderTaskDetailEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author howie
 * @email HowieWu94@foxmail.com
 * @date 2022-01-18 19:51:49
 */
public interface WareOrderTaskDetailService extends IService<WareOrderTaskDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


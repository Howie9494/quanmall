package com.howie.quanmall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.howie.common.utils.PageUtils;
import com.howie.quanmall.ware.entity.WareInfoEntity;
import com.howie.quanmall.ware.vo.FareVo;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author howie
 * @email HowieWu94@foxmail.com
 * @date 2022-01-18 19:51:49
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    FareVo getFare(Long addrId);
}


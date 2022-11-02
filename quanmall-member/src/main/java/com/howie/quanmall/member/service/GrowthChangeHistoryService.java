package com.howie.quanmall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.howie.common.utils.PageUtils;
import com.howie.quanmall.member.entity.GrowthChangeHistoryEntity;

import java.util.Map;

/**
 * 成长值变化历史记录
 *
 * @author howie
 * @email HowieWu94@foxmail.com
 * @date 2022-01-18 19:19:44
 */
public interface GrowthChangeHistoryService extends IService<GrowthChangeHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


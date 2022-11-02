package com.howie.quanmall.seckill.controller;

import com.howie.common.utils.R;
import com.howie.quanmall.seckill.service.SeckillService;
import com.howie.quanmall.seckill.to.SeckillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author Howie
 * @Date 2022/2/13 16:56
 * @Version 1.0
 */

@RestController
public class SeckillController {
    @Autowired
    SeckillService seckillService;

    /**
     * 返回当前时间可以参与的秒杀商品信息
     *  Created by Howie on 2022/2/13.
     */
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus(){
        List<SeckillSkuRedisTo> vos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(vos);
    }

    @GetMapping("/sku/seckill")
    public R getSkuSeckillInfo(@RequestParam("skuId") Long skuId){
        SeckillSkuRedisTo to = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(to);
    }

}

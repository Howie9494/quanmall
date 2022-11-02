package com.howie.quanmall.product.feign;

import com.howie.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author Howie
 * @Date 2022/2/13 19:39
 * @Version 1.0
 */

@FeignClient("quanmall-seckill")
public interface SeckillFeignService {

    @GetMapping("/sku/seckill")
    R getSkuSeckillInfo(@RequestParam("skuId")Long skuId);
}

package com.howie.quanmall.seckill.feign;

import com.howie.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Author Howie
 * @Date 2022/2/13 14:44
 * @Version 1.0
 */

@FeignClient("quanmall-coupon")
public interface CouponFeignService {

    @GetMapping("/coupon/seckillsession/getLatest3DaySession")
    R getLatest3DaySession();

}

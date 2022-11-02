package com.howie.quanmall.member.feign;

import com.howie.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author Howie
 * @Date 2022/1/19 14:42
 * @Version 1.0
 */

@FeignClient("quanmall-coupon")
public interface CouponFeignService {

    @RequestMapping("/coupon/coupon/member/list")
    public R memberCoupons();

}

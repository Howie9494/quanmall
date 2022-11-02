package com.howie.quanmall.authserver.feign;

import com.howie.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author Howie
 * @Date 2022/2/7 13:37
 * @Version 1.0
 */

@FeignClient("quanmall-third-party")
public interface ThirdPartyFeignService {

    @GetMapping("/sms/sendcode")
    R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}

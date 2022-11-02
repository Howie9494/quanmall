package com.howie.quanmall.order.feign;

import com.howie.quanmall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @Author Howie
 * @Date 2022/2/10 20:28
 * @Version 1.0
 */

@FeignClient("quanmall-cart")
public interface CartFeignService {

    @GetMapping("/currentUserCartItems")
    List<OrderItemVo> getCurrentUserCartItems();

}

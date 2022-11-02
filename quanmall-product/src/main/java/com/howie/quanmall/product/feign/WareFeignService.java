package com.howie.quanmall.product.feign;

import com.howie.common.to.SkuHasStockVo;
import com.howie.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author Howie
 * @Date 2022/1/26 13:47
 * @Version 1.0
 */
@FeignClient("quanmall-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hasstock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);
}

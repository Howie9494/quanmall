package com.howie.quanmall.product.feign;

import com.howie.common.to.es.SkuEsModel;
import com.howie.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author Howie
 * @Date 2022/1/26 14:41
 * @Version 1.0
 */
@FeignClient("quanmall-search")
public interface SearchFeignService {
    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}

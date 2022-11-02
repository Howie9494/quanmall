package com.howie.common.to;

import lombok.Data;

/**
 * @Author Howie
 * @Date 2022/1/26 13:41
 * @Version 1.0
 */
@Data
public class SkuHasStockVo {
    private Long skuId;
    private Boolean hasStock;
}

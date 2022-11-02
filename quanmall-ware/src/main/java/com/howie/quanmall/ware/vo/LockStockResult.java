package com.howie.quanmall.ware.vo;

import lombok.Data;

import java.util.PrimitiveIterator;

/**
 * @Author Howie
 * @Date 2022/2/11 16:36
 * @Version 1.0
 */

@Data
public class LockStockResult {
    private Long skuId;
    private Integer num;
    private Boolean locked;
}

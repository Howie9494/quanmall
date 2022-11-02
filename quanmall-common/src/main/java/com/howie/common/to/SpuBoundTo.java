package com.howie.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author Howie
 * @Date 2022/1/24 19:08
 * @Version 1.0
 */
@Data
public class SpuBoundTo {
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}

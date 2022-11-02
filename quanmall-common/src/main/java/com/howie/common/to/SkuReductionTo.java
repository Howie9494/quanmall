package com.howie.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author Howie
 * @Date 2022/1/24 19:17
 * @Version 1.0
 */
@Data
public class SkuReductionTo {
    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;
}

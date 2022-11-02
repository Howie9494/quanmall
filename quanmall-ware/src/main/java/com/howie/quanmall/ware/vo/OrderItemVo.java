package com.howie.quanmall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author Howie
 * @Date 2022/2/10 20:04
 * @Version 1.0
 */
@Data
public class OrderItemVo {
    private Long SkuId;
    private Boolean check;
    private String title;
    private String image;
    private List<String> skuAttr;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;
    private BigDecimal weight;
}

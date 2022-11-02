package com.howie.quanmall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author Howie
 * @Date 2022/2/3 14:35
 * @Version 1.0
 */
@Data
public class SkuItemSaleAttrVo{
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVo> attrValues;
}
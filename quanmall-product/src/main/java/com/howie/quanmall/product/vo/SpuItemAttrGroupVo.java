package com.howie.quanmall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author Howie
 * @Date 2022/2/3 14:35
 * @Version 1.0
 */
@Data
public class SpuItemAttrGroupVo{
    private String groupName;
    private List<SpuBaseAttrVo> attrs;
}
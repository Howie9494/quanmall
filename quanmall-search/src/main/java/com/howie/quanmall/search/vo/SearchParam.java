package com.howie.quanmall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面所有可能传递过来的查询条件
 * @Author Howie
 * @Date 2022/1/28 14:48
 * @Version 1.0
 */

@Data
public class SearchParam {

    private String keyword;//全文检索关键字
    private Long catalog3Id;//三级分类ID
    private String sort;//排序条件(销量、价格、热度评分)
    /**
     * 过滤条件
     * hasStock（是否有货）、skuPrice（价格区间）、brandId（品牌,可多选）、attrs（属性，可多选）
     *  Created by Howie on 2022/1/28.
     */
    private Integer hasStock;//hasStock=0/1
    private String skuPrice;//skuPrice={min}_{max}
    private List<Long> brandId;//brandId={a}&brandId={b}
    private List<String> attrs;//attrs={attrNo}_{attr1}:{attr2}

    private Integer pageNum=1;//页码

    private String _queryString;//原生的所有查询条件
}

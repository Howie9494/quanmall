package com.howie.quanmall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 2级分类vo
 * @Author Howie
 * @Date 2022/1/26 17:44
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Catalog2Vo {
    private String catalog1Id;
    private List<Catalog3Vo> catalog3List;
    private String id;
    private String name;

    /**
     * 3级分类vo
     *  Created by Howie on 2022/1/26.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Catalog3Vo{
        private String catalog2Id;
        private String id;
        private String name;
    }
}

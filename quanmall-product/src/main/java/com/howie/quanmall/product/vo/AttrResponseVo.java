package com.howie.quanmall.product.vo;

import lombok.Data;

/**
 * @Author Howie
 * @Date 2022/1/22 16:09
 * @Version 1.0
 */
@Data
public class AttrResponseVo extends AttrVo{

    private String catelogName;
    private String groupName;

    private Long[] catelogPath;
}

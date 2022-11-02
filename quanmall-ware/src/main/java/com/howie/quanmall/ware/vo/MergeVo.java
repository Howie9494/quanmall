package com.howie.quanmall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author Howie
 * @Date 2022/1/24 21:46
 * @Version 1.0
 */

@Data
public class MergeVo {

    private Long purchaseId;
    private List<Long> items;
}


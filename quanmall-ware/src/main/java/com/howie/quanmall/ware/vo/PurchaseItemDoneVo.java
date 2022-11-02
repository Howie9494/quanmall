package com.howie.quanmall.ware.vo;

import lombok.Data;

/**
 * @Author Howie
 * @Date 2022/1/25 10:41
 * @Version 1.0
 */

@Data
public class PurchaseItemDoneVo {
    private Long itemId;
    private Integer status;
    private String reason;
}

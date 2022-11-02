package com.howie.quanmall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author Howie
 * @Date 2022/1/25 10:40
 * @Version 1.0
 */
@Data
public class PurchaseDoneVo {
    @NotNull
    private Long id;
    private List<PurchaseItemDoneVo> items;
}

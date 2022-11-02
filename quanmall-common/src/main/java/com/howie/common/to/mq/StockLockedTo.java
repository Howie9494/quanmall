package com.howie.common.to.mq;

import lombok.Data;

import java.util.List;

/**
 * @Author Howie
 * @Date 2022/2/12 14:27
 * @Version 1.0
 */
@Data
public class StockLockedTo {
    private Long id;//库存工作单id
    private StockDetailTo detail;//工作单详情id
}

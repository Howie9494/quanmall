package com.howie.common.to.mq;

import lombok.Data;

/**
 * @Author Howie
 * @Date 2022/2/12 14:33
 * @Version 1.0
 */
@Data
public class StockDetailTo {
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * 购买个数
     */
    private Integer skuNum;
    /**
     * 工作单id
     */
    private Long taskId;

    private Long wareId;
    private Integer lockStatus;
}

package com.howie.quanmall.ware.exception;

/**
 * @Author Howie
 * @Date 2022/2/11 16:53
 * @Version 1.0
 */
public class NoStockException extends RuntimeException {
    private Long skuId;
    public NoStockException(Long skuId){
        super("商品："+skuId+"没有足够的库存");
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}

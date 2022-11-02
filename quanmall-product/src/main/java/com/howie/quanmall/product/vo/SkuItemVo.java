package com.howie.quanmall.product.vo;

import com.howie.quanmall.product.entity.SkuImagesEntity;
import com.howie.quanmall.product.entity.SkuInfoEntity;
import com.howie.quanmall.product.entity.SpuInfoDescEntity;
import com.howie.quanmall.product.service.SpuInfoDescService;
import lombok.Data;

import java.util.List;

/**
 * @Author Howie
 * @Date 2022/2/3 13:05
 * @Version 1.0
 */
@Data
public class SkuItemVo {
    SkuInfoEntity info;

    boolean hasStock = true;

    List<SkuImagesEntity> images;

    List<SkuItemSaleAttrVo> saleAttr;

    SpuInfoDescEntity desc;

    List<SpuItemAttrGroupVo> groupAttrs;

    SeckillInfoVo seckillInfo;//当前商品的秒杀优惠信息

}

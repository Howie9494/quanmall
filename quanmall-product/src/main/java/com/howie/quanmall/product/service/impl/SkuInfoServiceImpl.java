package com.howie.quanmall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.howie.common.to.SkuHasStockVo;
import com.howie.common.utils.R;
import com.howie.quanmall.product.entity.SkuImagesEntity;
import com.howie.quanmall.product.entity.SpuInfoDescEntity;
import com.howie.quanmall.product.feign.SeckillFeignService;
import com.howie.quanmall.product.feign.WareFeignService;
import com.howie.quanmall.product.service.*;
import com.howie.quanmall.product.vo.SeckillInfoVo;
import com.howie.quanmall.product.vo.SkuItemSaleAttrVo;
import com.howie.quanmall.product.vo.SkuItemVo;
import com.howie.quanmall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.howie.common.utils.PageUtils;
import com.howie.common.utils.Query;

import com.howie.quanmall.product.dao.SkuInfoDao;
import com.howie.quanmall.product.entity.SkuInfoEntity;
import org.springframework.util.StringUtils;

import static java.awt.SystemColor.info;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Autowired
    SkuImagesService imagesService;
    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    AttrGroupService attrGroupService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    WareFeignService wareFeignService;
    @Autowired
    SeckillFeignService seckillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            wrapper.and(w->{
                w.eq("sku_id",key).or().like("sku_name",key);
            });
        }
        String catalogId = (String) params.get("catalogId");
        if (!StringUtils.isEmpty(catalogId) && !"0".equalsIgnoreCase(catalogId)){
            wrapper.eq("catalog_id",catalogId);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }
        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)){
            wrapper.ge("price",min);
        }
        String max = (String) params.get("max");

        if (!StringUtils.isEmpty(max)){
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if(bigDecimal.compareTo(new BigDecimal(0))==1){
                    wrapper.le("price",max);
                }
            }catch (Exception e){

            }
        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> list = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id",spuId));
        return list;
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();
        //进行异步编排

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //sku基本信息获取
            SkuInfoEntity info = getById(skuId);
            skuItemVo.setInfo(info);
            return info;
        }, threadPoolExecutor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync(res -> {
            //获取spu销售属性组合
            List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            skuItemVo.setSaleAttr(saleAttrVos);
        }, threadPoolExecutor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync(res -> {
            //获取spu的介绍
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesc(spuInfoDescEntity);
        }, threadPoolExecutor);

        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync(res -> {
            //获取spu的规格参数信息
            List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(attrGroupVos);
        }, threadPoolExecutor);

        CompletableFuture<Void> stockFuture = infoFuture.thenAcceptAsync(res -> {
            R skusHasStock = wareFeignService.getSkusHasStock(Arrays.asList(res.getSkuId()));
            List<SkuHasStockVo> data = skusHasStock.getData(new TypeReference<List<SkuHasStockVo>>() {
            });
            Boolean hasStock = data.get(0).getHasStock();
            skuItemVo.setHasStock(hasStock);
        }, threadPoolExecutor);


        CompletableFuture<Void> imgFuture = CompletableFuture.runAsync(() -> {
            //sku图片信息获取
            List<SkuImagesEntity> images = imagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        }, threadPoolExecutor);

        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            //查询当前sku是否参与秒杀优惠
            R seckillInfo = seckillFeignService.getSkuSeckillInfo(skuId);
            if (seckillInfo.getCode() == 0) {
                SeckillInfoVo data = seckillInfo.getData(new TypeReference<SeckillInfoVo>() {
                });
                skuItemVo.setSeckillInfo(data);
            }
        },threadPoolExecutor);

        //等待所有任务都完成
        CompletableFuture.allOf(infoFuture,saleAttrFuture,descFuture,baseAttrFuture,imgFuture,stockFuture,seckillFuture).get();
        return skuItemVo;
    }

}
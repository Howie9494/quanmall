package com.howie.quanmall.ware.service.impl;

import com.howie.common.constant.WareConstant;
import com.howie.quanmall.ware.dao.PurchaseDao;
import com.howie.quanmall.ware.entity.PurchaseDetailEntity;
import com.howie.quanmall.ware.entity.PurchaseEntity;
import com.howie.quanmall.ware.feign.ProductFeignService;
import com.howie.quanmall.ware.service.PurchaseDetailService;
import com.howie.quanmall.ware.service.PurchaseService;
import com.howie.quanmall.ware.service.WareSkuService;
import com.howie.quanmall.ware.vo.MergeVo;
import com.howie.quanmall.ware.vo.PurchaseDoneVo;
import com.howie.quanmall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.howie.common.utils.PageUtils;
import com.howie.common.utils.Query;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {
    @Autowired
    PurchaseDetailService detailService;
    @Autowired
    WareSkuService wareSkuService;
    @Autowired
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status",0).or().eq("status",1)
        );
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null){
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        //TODO 确认采购单状态是0,1才可以合并

        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().filter(i->{
            Integer status = detailService.getById(i).getStatus();
            return status == WareConstant.PurchaseDetailStatusEnum.CREATED.getCode() ||
                    status == WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode();
        }).map(i -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(i);
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());
        if (collect != null && collect.size() != 0){
            detailService.updateBatchById(collect);
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(purchaseId);
            purchaseEntity.setUpdateTime(new Date());
            this.updateById(purchaseEntity);
        }
    }

    @Override
    public void received(List<Long> ids) {
        //确认当前采购单是新建或已分配状态
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity byId = this.getById(id);
            return byId;
        }).filter(item -> {
            if (item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                    item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).map(item->{
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());

        if (collect != null && collect.size() !=0){
            //改变采购单状态
            this.updateBatchById(collect);

            //改变采购项状态
            collect.forEach(item->{
                List<PurchaseDetailEntity> entities = detailService.listDetailByPurchaseId(item.getId());
                List<PurchaseDetailEntity> detailEntities = entities.stream().map(entity -> {
                    PurchaseDetailEntity entity1 = new PurchaseDetailEntity();
                    entity1.setId(entity.getId());
                    entity1.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                    return entity1;
                }).collect(Collectors.toList());
                detailService.updateBatchById(detailEntities);
            });
        }


    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo doneVo) {
        //改变采购单状态
        Long id = doneVo.getId();

        //改变采购项状态
        Boolean flag = true;
        List<PurchaseItemDoneVo> items = doneVo.getItems();
        List<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemDoneVo item : items){
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HAS_ERROR.getCode()){
                flag = false;
                detailEntity.setStatus(item.getStatus());
            }else {
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                //将成功采购的进行入库
                PurchaseDetailEntity byId = detailService.getById(item.getItemId());
                wareSkuService.addStock(byId.getSkuId(),byId.getWareId(),byId.getSkuNum());
            }
            detailEntity.setId(item.getItemId());
            updates.add(detailEntity);
        }
        detailService.updateBatchById(updates);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag?WareConstant.PurchaseStatusEnum.FINISH.getCode():WareConstant.PurchaseStatusEnum.HAS_ERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);


    }

}
package com.howie.quanmall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.howie.common.to.SkuHasStockVo;
import com.howie.common.to.mq.OrderTo;
import com.howie.common.to.mq.StockDetailTo;
import com.howie.common.to.mq.StockLockedTo;
import com.howie.common.utils.R;
import com.howie.quanmall.ware.entity.WareOrderTaskDetailEntity;
import com.howie.quanmall.ware.entity.WareOrderTaskEntity;
import com.howie.quanmall.ware.exception.NoStockException;
import com.howie.quanmall.ware.feign.OrderFeignService;
import com.howie.quanmall.ware.feign.ProductFeignService;
import com.howie.quanmall.ware.service.WareOrderTaskDetailService;
import com.howie.quanmall.ware.service.WareOrderTaskService;
import com.howie.quanmall.ware.vo.OrderItemVo;
import com.howie.quanmall.ware.vo.OrderVo;
import com.howie.quanmall.ware.vo.WareSkuLockVo;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.howie.common.utils.PageUtils;
import com.howie.common.utils.Query;

import com.howie.quanmall.ware.dao.WareSkuDao;
import com.howie.quanmall.ware.entity.WareSkuEntity;
import com.howie.quanmall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    WareSkuDao wareSkuDao;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    WareOrderTaskDetailService orderTaskDetailService;
    @Autowired
    WareOrderTaskService orderTaskService;
    @Autowired
    OrderFeignService orderFeignService;


    /**
     * 解锁库存
     * Created by Howie on 2022/2/12.
     */
    private void unlockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        wareSkuDao.unlockStock(skuId, wareId, num);
        WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity();
        taskDetailEntity.setId(taskDetailId);
        taskDetailEntity.setLockStatus(2);
        orderTaskDetailService.updateById(taskDetailEntity);
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entities == null || entities.size() == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStockLocked(0);
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0) {
                    wareSkuEntity.setSkuName((String) skuInfo.get("skuName"));
                }
            } catch (Exception e) {

            }
            wareSkuDao.insert(wareSkuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            Long count = wareSkuDao.getStock(skuId);
            vo.setSkuId(skuId);
            vo.setHasStock((count == null) ? false : (count > 0));
            return vo;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 锁定库存
     * Created by Howie on 2022/2/12.
     */
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        //保存库存工作单
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        orderTaskService.save(taskEntity);

        //查找那个仓库有库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            List<Long> wareId = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareId);
            return stock;
        }).collect(Collectors.toList());

        for (SkuWareHasStock hasStock : collect) {
            Boolean skuStocked = false;
            List<Long> wareIds = hasStock.getWareId();
            Long skuId = hasStock.getSkuId();
            if (wareIds == null || wareIds.size() == 0) {
                throw new NoStockException(skuId);
            }
            for (Long wareId : wareIds) {
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 1) {
                    skuStocked = true;
                    //发送消息 库存锁定成功
                    WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity(null, skuId, "", hasStock.getNum(), taskEntity.getId(), wareId, 1);
                    orderTaskDetailService.save(taskDetailEntity);
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(taskEntity.getId());
                    //只发id不行，防止回滚找不到数据
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(taskDetailEntity, stockDetailTo);
                    stockLockedTo.setDetail(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);

                    break;
                }
            }
            if (skuStocked == false) {
                throw new NoStockException(skuId);
            }
        }

        return true;

    }

    /**
     * 解锁库存
     *  Created by Howie on 2022/2/12.
     */
    @Transactional
    @Override
    public void unlockStock(StockLockedTo to) {

        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();

        WareOrderTaskDetailEntity byId = orderTaskDetailService.getById(detailId);
        if (byId != null) {
            //库存锁定成功，其他逻辑出现异常导致回滚。
            Long id = to.getId();
            WareOrderTaskEntity taskEntity = orderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();
            //根据订单号查询订单状态
            R orderStatus = orderFeignService.getOrderStatus(orderSn);
            if (orderStatus.getCode() == 0) {
                //订单数据返回成功
                OrderVo data = orderStatus.getData(new TypeReference<OrderVo>() {
                });
                if (data == null || data.getStatus() == 4) {
                    //订单被取消，解锁库存
                    //订单不存在，订单回滚
                    if (byId.getLockStatus() == 1){
                        unlockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                }
            }else {
                //解锁失败
                throw new RuntimeException("远程服务失败");
            }
        } else {
            //库存失败回滚-查询数据库关于这个订单的锁定库存信息，无需解锁
        }
    }

    @Transactional
    @Override
    public void unlockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        WareOrderTaskEntity entity = orderTaskService.getOrderTaskByOrderSn(orderSn);
        Long id = entity.getId();
        //按照工作单找到所有没有解锁的库存进行解锁
        List<WareOrderTaskDetailEntity> list = orderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", id).eq("lock_status", 1));
        for (WareOrderTaskDetailEntity taskDetailEntity : list) {
            unlockStock(taskDetailEntity.getSkuId(),taskDetailEntity.getWareId(),taskDetailEntity.getSkuNum(),taskDetailEntity.getId());
        }

    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}
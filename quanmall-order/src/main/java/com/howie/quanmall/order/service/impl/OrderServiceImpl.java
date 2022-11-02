package com.howie.quanmall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.howie.common.to.mq.OrderTo;
import com.howie.common.utils.R;
import com.howie.common.vo.MemberRespVo;
import com.howie.quanmall.order.constant.OrderConstant;
import com.howie.quanmall.order.dao.OrderItemDao;
import com.howie.quanmall.order.entity.OrderItemEntity;
import com.howie.quanmall.order.entity.PaymentInfoEntity;
import com.howie.quanmall.order.enume.OrderStatusEnum;
import com.howie.quanmall.order.feign.CartFeignService;
import com.howie.quanmall.order.feign.MemberFeignService;
import com.howie.quanmall.order.feign.ProductFeignService;
import com.howie.quanmall.order.feign.WareFeignService;
import com.howie.quanmall.order.interceptor.LoginUserInterceptor;
import com.howie.quanmall.order.service.OrderItemService;
import com.howie.quanmall.order.service.PaymentInfoService;
import com.howie.quanmall.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.howie.common.utils.PageUtils;
import com.howie.common.utils.Query;

import com.howie.quanmall.order.dao.OrderDao;
import com.howie.quanmall.order.entity.OrderEntity;
import com.howie.quanmall.order.service.OrderService;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    CartFeignService cartFeignService;
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    WareFeignService wareFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    PaymentInfoService paymentInfoService;

    private ThreadLocal<OrderSubmitVo> orderSubmitVoThreadLocal = new ThreadLocal<>();

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //远程查询所有的收货地址列表
            List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId());
            confirmVo.setAddress(address);
        }, executor);

        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //远程查询购物车所有选中的购物项
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(items);
        }, executor).thenRunAsync(()->{
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R hasStock = wareFeignService.getSkusHasStock(collect);
            List<SkuStockVo> data = hasStock.getData(new TypeReference<List<SkuStockVo>>() {
            });
            if (data!=null){
                Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                confirmVo.setStocks(map);
            }
        },executor);

        //查询用户积分
        Integer integration = memberRespVo.getIntegration();
        confirmVo.setIntegration(integration);

        //防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        confirmVo.setOrderToken(token);
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberRespVo.getId(),token,30, TimeUnit.MINUTES);

        try {
            CompletableFuture.allOf(getAddressFuture,cartFuture).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return confirmVo;
    }

    //@GlobalTransactional
    @Transactional
    @Override
    public SubmitOrderRespVo submitOrder(OrderSubmitVo vo) {
        SubmitOrderRespVo respVo = new SubmitOrderRespVo();
        respVo.setCode(0);
        orderSubmitVoThreadLocal.set(vo);
        //验证令牌
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();
        String redisKey = OrderConstant.USER_ORDER_TOKEN_PREFIX + LoginUserInterceptor.loginUser.get().getId();
        Long execute = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(redisKey), orderToken);
        if (execute == 1L){
            //令牌验证成功
            //创建订单
            OrderCreateTo order = createOrder();
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if(Math.abs(payAmount.subtract(payPrice).doubleValue())<0.01){
                //金额对比成功
                //保存订单到数据库
                saveOrder(order);
                //锁定库存
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> collect = order.getOrderItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                wareSkuLockVo.setLocks(collect);
                R r = wareFeignService.orderLockStock(wareSkuLockVo);
                if (r.getCode()==0){
                    //锁定成功
                    respVo.setOrder(order.getOrder());
                    //int i = 10/0;
                    //订单创建成功发送消息给mq
                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order.getOrder());

                    return respVo;
                }else {
                    //锁定失败
                    respVo.setCode(3);
                    return respVo;
                }
            }else {
                respVo.setCode(2);
                return respVo;
            }
        }else {
            //令牌验证失败
            respVo.setCode(1);
            return respVo;
        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        OrderEntity orderEntity = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return orderEntity;
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        //查询当前订单最新状态
        OrderEntity orderEntity = this.getById(entity.getId());
        if (orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()){
            //关闭订单
            OrderEntity update = new OrderEntity();
            update.setId(orderEntity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);

            //为了避免延迟，取消订单时主动发消息给解锁库存的监听器
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity,orderTo);

            try{
                //每个发送的消息都做好日志记录
                rabbitTemplate.convertAndSend("order-event-exchange","order.release.other.#",orderTo);
            }catch (Exception e){
                //将未发送的消息进行重试发送
            }

        }
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity order = this.getOrderByOrderSn(orderSn);

        payVo.setOut_trade_no(order.getOrderSn());//订单号
        payVo.setSubject("趣安商城");//订单主题
        payVo.setTotal_amount(order.getPayAmount().setScale(2,BigDecimal.ROUND_DOWN).toString());//订单金额
        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id",memberRespVo.getId()).orderByDesc("id")
        );

        List<OrderEntity> order_sn1 = page.getRecords().stream().map(order -> {
            List<OrderItemEntity> order_sn = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
            order.setItemEntities(order_sn);
            return order;
        }).collect(Collectors.toList());

        page.setRecords(order_sn1);

        return new PageUtils(page);
    }

    /**
     * 处理支付宝的支付结果
     *  Created by Howie on 2022/2/13.
     */
    @Override
    public String handlePayResult(PayAsyncVo vo) {
        //保存交易流水
        PaymentInfoEntity infoEntity = new PaymentInfoEntity();
        infoEntity.setAlipayTradeNo(vo.getTrade_no());
        infoEntity.setOrderSn(vo.getOut_trade_no());
        infoEntity.setPaymentStatus(vo.getTrade_status());
        infoEntity.setCallbackTime(vo.getNotify_time());

        paymentInfoService.save(infoEntity);

        //修改订单状态信息
        if (vo.getTrade_status().equals("TRADE_SUCCESS")||vo.getTrade_status().equals("TRADE_FINISHED")){
            //支付成功
            String outTradeNo = vo.getOut_trade_no();
            this.baseMapper.updateOrderStatus(outTradeNo,OrderStatusEnum.PAYED.getCode());
        }

        return "success";
    }


    /**
     * 保存订单数据
     *  Created by Howie on 2022/2/11.
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);

        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);

    }

    private OrderCreateTo createOrder(){
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        //生成订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = buildOrder(orderSn);
        orderCreateTo.setOrder(orderEntity);


        //获取所有订单项
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);
        orderCreateTo.setOrderItems(itemEntities);

        //验证价格
        computePrice(orderEntity,itemEntities);

        return orderCreateTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        BigDecimal total = new BigDecimal("0.00");
        BigDecimal coupon = new BigDecimal("0.00");
        BigDecimal integration = new BigDecimal("0.00");
        BigDecimal promotion = new BigDecimal("0.00");

        BigDecimal giftGrowth = new BigDecimal("0.00");
        BigDecimal giftIntegration = new BigDecimal("0.00");

        //订单价格相关数据
        for (OrderItemEntity itemEntity : itemEntities) {
            coupon = coupon.add(itemEntity.getCouponAmount());
            integration = integration.add(itemEntity.getIntegrationAmount());
            promotion = promotion.add(itemEntity.getPromotionAmount());
            total = total.add(itemEntity.getRealAmount());
            giftGrowth = giftGrowth.add(new BigDecimal(itemEntity.getGiftGrowth().toString()));
            giftIntegration = giftIntegration.add(new BigDecimal(itemEntity.getGiftIntegration().toString()));
        }
        orderEntity.setTotalAmount(total);
        //应付总额
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));

        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);

        orderEntity.setGrowth(giftGrowth.intValue());
        orderEntity.setIntegration(giftIntegration.intValue());

        orderEntity.setDeleteStatus(0);
    }

    /**
     * 构建订单项
     *  Created by Howie on 2022/2/11.
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
        if (items!=null && items.size()>0){
            List<OrderItemEntity> itemEntities = items.stream().map(item -> {
                OrderItemEntity orderItemEntity = buildOrderItem(item);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return itemEntities;
        }
        return null;
    }

    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        //spu信息
        Long skuId = item.getSkuId();
        R spuInfoBySkuId = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = spuInfoBySkuId.getData(new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setSpuId(data.getId());
        orderItemEntity.setSpuBrand(data.getBrandId().toString());
        orderItemEntity.setSpuName(data.getSpuName());
        orderItemEntity.setCategoryId(data.getCatalogId());

        //sku信息
        orderItemEntity.setSkuId(item.getSkuId());
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuPrice(item.getPrice());
        orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(item.getSkuAttr(),";"));
        orderItemEntity.setSkuQuantity(item.getCount());

        //积分信息
        orderItemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());
        orderItemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());

        //价格信息
        orderItemEntity.setPromotionAmount(new BigDecimal("0.00"));
        orderItemEntity.setCouponAmount(new BigDecimal("0.00"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0.00"));
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        BigDecimal real = origin.subtract(orderItemEntity.getCouponAmount()).subtract(orderItemEntity.getPromotionAmount()).subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(real);


        return orderItemEntity;
    }


    private OrderEntity buildOrder(String orderSn){
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);
        orderEntity.setMemberId(memberRespVo.getId());

        //获取收货地址
        OrderSubmitVo orderSubmitVo = orderSubmitVoThreadLocal.get();
        R fare = wareFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo data = fare.getData(new TypeReference<FareVo>() {
        });
        orderEntity.setFreightAmount(data.getFare());
        orderEntity.setReceiverCity(data.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(data.getAddress().getDetailAddress());
        orderEntity.setReceiverName(data.getAddress().getName());
        orderEntity.setReceiverPhone(data.getAddress().getPhone());
        orderEntity.setReceiverPostCode(data.getAddress().getPostCode());
        orderEntity.setReceiverProvince(data.getAddress().getProvince());
        orderEntity.setReceiverRegion(data.getAddress().getRegion());

        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setConfirmStatus(7);

        return orderEntity;
    }
}
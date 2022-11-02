package com.howie.quanmall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.howie.common.utils.R;
import com.howie.quanmall.seckill.feign.CouponFeignService;
import com.howie.quanmall.seckill.feign.ProductFeignService;
import com.howie.quanmall.seckill.service.SeckillService;
import com.howie.quanmall.seckill.to.SeckillSkuRedisTo;
import com.howie.quanmall.seckill.vo.SeckillSessionsWithSkus;
import com.howie.quanmall.seckill.vo.SeckillSkuVo;
import com.howie.quanmall.seckill.vo.SkuInfoVo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author Howie
 * @Date 2022/2/13 14:43
 * @Version 1.0
 */

@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RedissonClient redissonClient;

    private static final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    private static final String SKUKILL_CACHE_PREFIX = "seckill:skus";
    private static final String SKU_STOCK_SEMAPHORE = "seckill:stock:";

    @Override
    public void uploadSeckillSkuLatest3Days() {
        //扫描参与秒杀的活动
        R session = couponFeignService.getLatest3DaySession();
        if (session.getCode()==0){
            List<SeckillSessionsWithSkus> data = session.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
            });
            //缓存活动信息
            saveSessionInfos(data);
            //缓存活动关联商品信息
            saveSessionSkuInfos(data);
        }
    }

    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        //确定当前时间数据哪个秒杀场次
        long time = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        for (String key : keys) {
            String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
            String[] s = replace.split("_");
            long start = Long.parseLong(s[0]);
            long end = Long.parseLong(s[1]);
            if (time>=start && time<=end){
                //获取这个秒杀场次需要的所有商品信息
                List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                List<String> list = ops.multiGet(range);
                if (list!=null){
                    List<SeckillSkuRedisTo> collect = list.stream().map(item -> {
                        SeckillSkuRedisTo redisTo = JSON.parseObject(item.toString(), SeckillSkuRedisTo.class);
                        //redisTo.setRandomCode(null);
                        return redisTo;
                    }).collect(Collectors.toList());
                    return collect;
                }
                break;
            }
        }
        return null;
    }

    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        if (keys!=null && keys.size()>0){
            String regx = "\\d_"+skuId;
            for (String key : keys) {
                if (Pattern.matches(regx,key)){
                    String s = hashOps.get(key);
                    SeckillSkuRedisTo redisTo = JSON.parseObject(s, SeckillSkuRedisTo.class);
                    long time = new Date().getTime();
                    if (time>=redisTo.getStartTime() && time<=redisTo.getEndTime()){

                    }else {
                        redisTo.setRandomCode(null);
                    }
                    return redisTo;
                }
            }
        }
        return null;
    }

    /**
     * 缓存活动信息
     *  Created by Howie on 2022/2/13.
     */
    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessions){
        sessions.stream().forEach(session->{
            long start = session.getStartTime().getTime();
            long end = session.getEndTime().getTime();

            String key = SESSIONS_CACHE_PREFIX + start + "_" + end;
            Boolean hasKey = redisTemplate.hasKey(key);

            if (!hasKey){
                List<String> collect = session.getRelationSkus().stream().map(item -> item.getId().toString()+"_"+item.getSkuId().toString()).collect(Collectors.toList());
                redisTemplate.opsForList().leftPushAll(key,collect);
            }

        });
    }

    /**
     * 缓存活动关联商品信息
     *  Created by Howie on 2022/2/13.
     */
    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessions){
        sessions.stream().forEach(session->{
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            session.getRelationSkus().stream().forEach(seckillSkuVo -> {
                Boolean hasKey = ops.hasKey(seckillSkuVo.getId().toString()+"_"+seckillSkuVo.getSkuId().toString());
                String token = UUID.randomUUID().toString().replace("_", "");

                if (!hasKey){

                    //缓存商品信息
                    SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
                    //sku基本数据
                    R info = productFeignService.info(seckillSkuVo.getSkuId());
                    if (info.getCode()==0){
                        SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        redisTo.setSkuInfo(skuInfo);
                    }

                    //sku秒杀数据
                    BeanUtils.copyProperties(seckillSkuVo,redisTo);

                    //保存随机码和时间
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());
                    redisTo.setRandomCode(token);

                    String s = JSON.toJSONString(redisTo);
                    ops.put(seckillSkuVo.getId().toString()+"_"+seckillSkuVo.getSkuId().toString(),s);


                    //使用库存作为信号量  限流
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    semaphore.trySetPermits(Integer.parseInt(seckillSkuVo.getSeckillCount().toString()));
                }

            });
        });
    }
}

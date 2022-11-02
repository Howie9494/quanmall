package com.howie.quanmall.seckill.scheduled;

import com.howie.quanmall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @Author Howie
 * @Date 2022/2/13 14:21
 * @Version 1.0
 */
@Slf4j
@Service
@EnableAsync
@EnableScheduling
public class SeckillSchedule {
    @Autowired
    SeckillService seckillService;
    @Autowired
    RedissonClient redissonClient;

    private static final String UPLOAD_LOCK = "seckill:upload:lock";

    //每天晚上3点上架最近三天需要秒杀的商品
    @Scheduled(cron = "0 0 3 * * ?")
    public void uploadSeckillSkuLatest3Days(){
        //重复上架无需处理
        RLock lock = redissonClient.getLock(UPLOAD_LOCK);
        lock.lock(30, TimeUnit.SECONDS);
        try{
            log.info("秒杀商品信息上架...");
            seckillService.uploadSeckillSkuLatest3Days();
        }finally {
            lock.unlock();
        }
    }
}

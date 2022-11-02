package com.howie.quanmall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.howie.common.utils.R;
import com.howie.quanmall.ware.feign.MemberFeignService;
import com.howie.quanmall.ware.vo.FareVo;
import com.howie.quanmall.ware.vo.MemberAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.howie.common.utils.PageUtils;
import com.howie.common.utils.Query;

import com.howie.quanmall.ware.dao.WareInfoDao;
import com.howie.quanmall.ware.entity.WareInfoEntity;
import com.howie.quanmall.ware.service.WareInfoService;
import org.springframework.util.StringUtils;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {
    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            wrapper.eq("id",key).or().like("name",key)
                    .or().like("address",key).or().like("areacode",key);
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        R info = memberFeignService.info(addrId);
        MemberAddressVo data = info.getData("memberReceiveAddress",new TypeReference<MemberAddressVo>() {
        });
        if (data!=null){
            if (data.getProvince().equals("浙江省") || data.getProvince().equals("上海市")){
                BigDecimal fare = new BigDecimal("8");
                fareVo.setAddress(data);
                fareVo.setFare(fare);
                return fareVo;
            }else {
                BigDecimal fare = new BigDecimal("10");
                fareVo.setAddress(data);
                fareVo.setFare(fare);
                return fareVo;
            }
        }
        return null;
    }

}
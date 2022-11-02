package com.howie.quanmall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author Howie
 * @Date 2022/2/11 13:40
 * @Version 1.0
 */

@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}

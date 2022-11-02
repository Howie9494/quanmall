package com.howie.quanmall.order.vo;

import com.howie.quanmall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @Author Howie
 * @Date 2022/2/11 15:07
 * @Version 1.0
 */

@Data
public class SubmitOrderRespVo {

    private OrderEntity order;
    private Integer code;

}

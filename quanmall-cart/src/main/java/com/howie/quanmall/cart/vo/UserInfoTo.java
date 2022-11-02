package com.howie.quanmall.cart.vo;

import lombok.Data;

/**
 * @Author Howie
 * @Date 2022/2/9 13:36
 * @Version 1.0
 */
@Data
public class UserInfoTo {

    private Long userId;
    private String userKey;

    private boolean tempUser = false;
}

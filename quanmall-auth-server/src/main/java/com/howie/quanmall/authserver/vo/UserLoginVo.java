package com.howie.quanmall.authserver.vo;

import lombok.Data;

/**
 * @Author Howie
 * @Date 2022/2/7 19:17
 * @Version 1.0
 */

@Data
public class UserLoginVo {

    private String loginAcct;
    private String password;
    private String url;
}

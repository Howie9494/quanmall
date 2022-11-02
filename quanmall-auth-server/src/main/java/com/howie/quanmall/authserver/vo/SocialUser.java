package com.howie.quanmall.authserver.vo;

import lombok.Data;

/**
 * @Author Howie
 * @Date 2022/2/8 13:52
 * @Version 1.0
 */
@Data
public class SocialUser {

    private String access_token;
    private String remind_in;
    private long expires_in;
    private String uid;
    private String isRealName;

}

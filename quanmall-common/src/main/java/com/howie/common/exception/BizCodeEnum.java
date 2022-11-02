package com.howie.common.exception;

/**
 * @Author Howie
 * @Date 2022/1/21 19:40
 * @Version 1.0
 */
public enum BizCodeEnum {
    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    VALID_EXCEPTION(10001,"参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002,"验证码获取频率过高，请稍后再试"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常"),
    USER_EXIST_EXCEPTION(15001,"用户名已经存在"),
    PHONE_EXIST_EXCEPTION(15002,"手机号已经存在"),
    LOGINACCT_PASSWORD_INVALID_EXCEPTION(15003,"账号或密码错误"),
    NO_STOCK_EXCEPTION(21000,"商品库存不足"),
    OAUTH_LOGIN_EXCEPTION(15004,"授权登录失败");

    private int code;
    private String message;

    BizCodeEnum(int code,String msg){
        this.code = code;
        this.message = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

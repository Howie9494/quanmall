package com.howie.quanmall.member.exception;

/**
 * @Author Howie
 * @Date 2022/2/7 16:46
 * @Version 1.0
 */
public class PhoneExistException extends RuntimeException {

    public PhoneExistException(){
        super("手机号已存在");
    }
}

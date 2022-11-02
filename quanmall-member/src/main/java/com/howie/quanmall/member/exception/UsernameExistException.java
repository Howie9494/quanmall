package com.howie.quanmall.member.exception;

/**
 * @Author Howie
 * @Date 2022/2/7 16:46
 * @Version 1.0
 */
public class UsernameExistException extends RuntimeException {

    public UsernameExistException(){
        super("用户名已存在");
    }
}

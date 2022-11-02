package com.howie.quanmall.member.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @Author Howie
 * @Date 2022/2/7 14:57
 * @Version 1.0
 */
@Data
public class MemberRegistVo {

    private String userName;

    private String password;

    private String phone;
}

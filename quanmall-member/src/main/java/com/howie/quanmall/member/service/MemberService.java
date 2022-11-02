package com.howie.quanmall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.howie.common.utils.PageUtils;
import com.howie.quanmall.member.entity.MemberEntity;
import com.howie.quanmall.member.exception.PhoneExistException;
import com.howie.quanmall.member.exception.UsernameExistException;
import com.howie.quanmall.member.vo.MemberLoginVo;
import com.howie.quanmall.member.vo.MemberRegistVo;
import com.howie.quanmall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author howie
 * @email HowieWu94@foxmail.com
 * @date 2022-01-18 19:19:44
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo vo);

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkUserNameUnique(String userName) throws UsernameExistException;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity login(SocialUser socialUser) throws Exception;
}



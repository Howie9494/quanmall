package com.howie.quanmall.member.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.howie.common.exception.BizCodeEnum;
import com.howie.quanmall.member.exception.PhoneExistException;
import com.howie.quanmall.member.exception.UsernameExistException;
import com.howie.quanmall.member.feign.CouponFeignService;
import com.howie.quanmall.member.vo.MemberLoginVo;
import com.howie.quanmall.member.vo.MemberRegistVo;
import com.howie.quanmall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.howie.quanmall.member.entity.MemberEntity;
import com.howie.quanmall.member.service.MemberService;
import com.howie.common.utils.PageUtils;
import com.howie.common.utils.R;



/**
 * 会员
 *
 * @author howie
 * @email HowieWu94@foxmail.com
 * @date 2022-01-18 19:19:44
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;

    @RequestMapping("/coupons")
    public R test(){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        R r = couponFeignService.memberCoupons();
        return R.ok().put("member",memberEntity).put("coupons",r.get("coupons"));
    }

    @PostMapping("/oauth2/login")
    public R oauthLogin(@RequestBody SocialUser socialUser) throws Exception {
        MemberEntity entity = memberService.login(socialUser);
        if (entity!=null){
            return R.ok().setData(entity);
        }else {
            return R.error(BizCodeEnum.OAUTH_LOGIN_EXCEPTION.getCode(),
                    BizCodeEnum.OAUTH_LOGIN_EXCEPTION.getMessage());
        }
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo){
        MemberEntity entity = memberService.login(vo);
        if (entity!=null){
            return R.ok().setData(entity);
        }else {
            return R.error(BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getCode(),
                    BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getMessage());
        }
    }

    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo vo){
        try{
            memberService.regist(vo);
        }catch (PhoneExistException e){
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(),BizCodeEnum.PHONE_EXIST_EXCEPTION.getMessage());
        }catch (UsernameExistException e){
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(),BizCodeEnum.USER_EXIST_EXCEPTION.getMessage());
        }
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}

package com.howie.quanmall.authserver.controller;

import com.alibaba.fastjson.TypeReference;
import com.howie.common.constant.AuthServerConstant;
import com.howie.common.exception.BizCodeEnum;
import com.howie.common.utils.R;
import com.howie.common.vo.MemberRespVo;
import com.howie.quanmall.authserver.feign.MemberFeignService;
import com.howie.quanmall.authserver.feign.ThirdPartyFeignService;
import com.howie.quanmall.authserver.vo.UserLoginVo;
import com.howie.quanmall.authserver.vo.UserRegistVo;
import com.netflix.ribbon.proxy.annotation.Http;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author Howie
 * @Date 2022/2/6 16:01
 * @Version 1.0
 */

@Controller
public class AuthController {
    @Autowired
    ThirdPartyFeignService thirdPartyFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    MemberFeignService memberFeignService;

    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone){
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone);
        if (!StringUtils.isEmpty(redisCode)){
            long time = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - time < 60000){
                //60s内不能再发
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(),BizCodeEnum.SMS_CODE_EXCEPTION.getMessage());
            }
            String code = redisCode.split("_")[0];
            thirdPartyFeignService.sendCode(phone,code);
            //System.out.println(code);
            return R.ok();
        }
        String code = UUID.randomUUID().toString().substring(0,5)+"_"+System.currentTimeMillis();
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,code,5, TimeUnit.MINUTES);
        code = code.split("_")[0];
        thirdPartyFeignService.sendCode(phone,code);
        //System.out.println(code);
        return R.ok();
    }

    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes){

        if (result.hasErrors()){
            Map<String,String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField,FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.quanmall.com/reg.html";
        }

        String code = vo.getCode();
        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (!StringUtils.isEmpty(s)){
            if (code.equals(s.split("_")[0])){
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                //验证码通过
                R r = memberFeignService.regist(vo);
                if (r.getCode() == 0){
                    return "redirect:http://auth.quanmall.com/login.html";
                }else {
                    Map<String,String> errors = new HashMap<>();
                    errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
                    redirectAttributes.addFlashAttribute("errors",errors);
                    return "redirect:http://auth.quanmall.com/reg.html";
                }
            }else {
                Map<String,String> errors = new HashMap<>();
                errors.put("code","验证码错误");
                redirectAttributes.addFlashAttribute("errors",errors);
                return "redirect:http://auth.quanmall.com/reg.html";
            }
        }else {
            Map<String,String> errors = new HashMap<>();
            errors.put("code","验证码错误");
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.quanmall.com/reg.html";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session){
        R login = memberFeignService.login(vo);
        if (login.getCode()==0){
            MemberRespVo data = login.getData("data", new TypeReference<MemberRespVo>() {
            });
            session.setAttribute(AuthServerConstant.LOGIN_USER,data);
            return "redirect:"+vo.getUrl();
        }else {
            Map<String,String> errors = new HashMap<>();
            errors.put("msg",login.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.quanmall.com/login.html";
        }
    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession session,@RequestParam(value = "url",required = false) String returnUrl){
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute == null){
            return "login";
        }else {
            return "redirect:"+(returnUrl==null?"http://quanmall.com":returnUrl);
        }
    }

    @GetMapping("/loginOut")
    public String loginOut(HttpSession session,@RequestParam("url") String returnUrl){
        session.removeAttribute(AuthServerConstant.LOGIN_USER);
        //System.out.println(returnUrl);
        return "redirect:" + returnUrl;
    }
}
